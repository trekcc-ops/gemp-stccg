package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardVisitor;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@JsonSerialize(using = GameStateSerializer.class)
public abstract class GameState {
    private static final Logger LOGGER = LogManager.getLogger(GameState.class);

    // previousZoneSizes and previousPlayerScores are only used for GameStats comparisons and event sending
    private Map<String, Map<Zone, Integer>> _previousZoneSizes = new HashMap<>();
    private Map<String, Integer> _previousPlayerScores = new HashMap<>();

    PlayerOrder _playerOrder;
    private ModifiersLogic _modifiersLogic;
    protected final Map<Zone, Map<String, List<PhysicalCard>>> _cardGroups = new EnumMap<>(Zone.class);
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    protected final Map<Integer, PhysicalCard> _allCards = new HashMap<>();
    Phase _currentPhase;
    private boolean _consecutiveAction;
    final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();
    int _nextCardId = 1;
    final Map<String, Integer> _turnNumbers = new HashMap<>();
    Map<String, Integer> _playerScores = new HashMap<>();
    Map<String, Player> _players = new HashMap<>();
    private ActionsEnvironment _actionsEnvironment;

    protected GameState(DefaultGame game, Iterable<String> playerIds) {
        Collection<Zone> cardGroupList = new LinkedList<>();
        cardGroupList.add(Zone.DRAW_DECK);
        cardGroupList.add(Zone.HAND);
        cardGroupList.add(Zone.VOID);
        cardGroupList.add(Zone.VOID_FROM_HAND);
        cardGroupList.add(Zone.DISCARD);
        cardGroupList.add(Zone.REMOVED);

        cardGroupList.forEach(cardGroup -> _cardGroups.put(cardGroup, new HashMap<>()));
        for (String playerId : playerIds) {
            cardGroupList.forEach(cardGroup -> _cardGroups.get(cardGroup).put(playerId, new LinkedList<>()));
            _turnNumbers.put(playerId, 0);
            _playerScores.put(playerId, 0);
            _players.put(playerId, new Player(game, playerId));
        }
        _modifiersLogic = new ModifiersLogic(game);
        _actionsEnvironment = new DefaultActionsEnvironment(game);
    }

    public abstract DefaultGame getGame();

    public void initializePlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(playerOrder.getFirstPlayer());
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard();
        }
    }

    public void loadPlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
    }
    public ActionsEnvironment getActionsEnvironment() { return _actionsEnvironment; }

    public void finish() {
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.sendEvent(GameEvent.Type.GAME_ENDED);
        }

        if(_playerOrder == null || _playerOrder.getAllPlayers() == null)
            return;

        for (String playerId : _playerOrder.getAllPlayers()) {
            for(var card : getDrawDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    sendCreatedCardToListener(card, false, listener, true, true);
                }
            }
        }
    }

    public boolean isConsecutiveAction() {
        return _consecutiveAction;
    }

    public void setConsecutiveAction(boolean consecutiveAction) {
        _consecutiveAction = consecutiveAction;
    }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }

    Collection<GameStateListener> getAllGameStateListeners() {
        return getGame().getAllGameStateListeners();
    }

    public void sendGameStateToClient(String playerId, GameStateListener listener, boolean restoreSnapshot) {
        if (_playerOrder != null) {
            listener.initializeBoard();
            if (getCurrentPlayerId() != null) listener.setCurrentPlayerId(getCurrentPlayerId());
            if (_currentPhase != null) listener.setCurrentPhase(_currentPhase);

            sendCardsToClient(playerId, listener, restoreSnapshot);
        }
        for (String lastMessage : getMessages())
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        sendAwaitingDecisionToListener(listener, playerId, awaitingDecision);
    }

    protected void sendCardsToClient(String playerId, GameStateListener listener, boolean restoreSnapshot) {

        Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(_inPlay);
        Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

        do {
            Iterator<PhysicalCard> cardIterator = cardsLeftToSend.iterator();
            while (cardIterator.hasNext()) {
                PhysicalCard physicalCard = cardIterator.next();
                PhysicalCard attachedTo = physicalCard.getAttachedTo();
                if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                    sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
                    sentCardsFromPlay.add(physicalCard);
                    cardIterator.remove();
                }
            }
        } while (!cardsLeftToSend.isEmpty());

        Collection<PhysicalCard> cardsPutIntoPlay = new LinkedList<>();

        cardsPutIntoPlay.addAll(_cardGroups.get(Zone.HAND).get(playerId));
        cardsPutIntoPlay.addAll(_cardGroups.get(Zone.DISCARD).get(playerId));
        for (PhysicalCard physicalCard : cardsPutIntoPlay) {
            sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
        }

        listener.sendEvent(new GameEvent(GameEvent.Type.GAME_STATS, this));
    }

    public void sendMessage(String message) {
        getGame().addMessage(message);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendMessage(message);
    }

    public void playerDecisionStarted(String playerId, AwaitingDecision awaitingDecision) {
        _playerDecisions.put(playerId, awaitingDecision);
        for (GameStateListener listener : getAllGameStateListeners())
            sendAwaitingDecisionToListener(listener, playerId, awaitingDecision);
    }

    private void sendAwaitingDecisionToListener(GameStateListener listener, String playerId, AwaitingDecision decision) {
        if (decision != null)
            listener.decisionRequired(playerId, decision);
    }

    public void playerDecisionFinished(String playerId, UserFeedback userFeedback) {
        userFeedback.removeDecision(playerId);
        _playerDecisions.remove(playerId);
    }

    public void playerDecisionFinished(String playerId) {
        getGame().getUserFeedback().removeDecision(playerId);
        _playerDecisions.remove(playerId);
    }

    public void transferCard(PhysicalCard card, PhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            card.setZone(Zone.ATTACHED);

        card.attachTo(transferTo);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.MOVE_CARD_IN_PLAY,card));
    }

    public void detachCard(PhysicalCard attachedCard, Zone newZone) {
        attachedCard.setZone(newZone);
        attachedCard.detach();
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.MOVE_CARD_IN_PLAY,attachedCard));
    }

    public void attachCard(PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        card.attachTo(attachTo);
        addCardToZone(card, Zone.ATTACHED);
    }

    public void cardAffectsCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCards) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.CARD_AFFECTED_BY_CARD, card, affectedCards, getPlayer(playerPerforming)));
    }

    public void activatedCard(String playerPerforming, PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners()) {
            GameEvent event = new GameEvent(GameEvent.Type.FLASH_CARD_IN_PLAY, card, getPlayer(playerPerforming));
            listener.sendEvent(event);
        }
    }

    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        List<PhysicalCard> zoneCards = _cardGroups.get(zone).get(playerId);
        if (zoneCards != null)
            return zoneCards;
        else
            return _inPlay;
    }

    public void removeCardFromZone(PhysicalCard card) {
        removeCardsFromZone(card.getOwnerName(), Collections.singleton(card));
    }

    public void moveCard(PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.MOVE_CARD_IN_PLAY, card));
    }


    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            List<PhysicalCard> zoneCards = getZoneCards(card.getOwnerName(), card.getZone());
            if (!zoneCards.contains(card) && card.getZone() != Zone.VOID)
                LOGGER.error(
                        "Card was not found in the expected zone: " + card.getTitle() + ", " + card.getZone().name());
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) card.stopAffectingGame(getGame());

            getZoneCards(card.getOwnerName(), zone).remove(card);

            if (card instanceof PhysicalReportableCard1E reportable) {
                if (reportable.getAwayTeam() != null) {
                    reportable.leaveAwayTeam();
                }
            }

            if (zone.isInPlay())
                _inPlay.remove(card);
            if (zone == Zone.ATTACHED)
                card.attachTo(null);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {

            Set<PhysicalCard> removedCardsVisibleByPlayer = new HashSet<>();
            for (PhysicalCard card : cards) {
                boolean publicDiscard = card.getZone() == Zone.DISCARD && getGame().isDiscardPilePublic();
                if (card.getZone().isPublic() || publicDiscard ||
                        (card.getZone().isVisibleByOwner() && card.getOwnerName().equals(listener.getPlayerId())))
                    removedCardsVisibleByPlayer.add(card);
            }
            if (!removedCardsVisibleByPlayer.isEmpty())
                listener.sendEvent(new GameEvent(GameEvent.Type.REMOVE_CARD_FROM_PLAY, removedCardsVisibleByPlayer, getPlayer(playerPerforming)));
        }


        for (PhysicalCard card : cards) {
            card.setZone(null);
        }
    }

    public void addCardToZone(PhysicalCard card, Zone zone) {
        addCardToZone(card, zone, true);
    }
    public void addCardToZone(PhysicalCard card, Zone zone, EndOfPile endOfPile) {
        addCardToZone(card, zone, endOfPile != EndOfPile.TOP);
    }

    public void addCardToZone(PhysicalCard card, Zone zone, boolean end) {
        addCardToZone(card, zone, end, false);
    }

    public void addCardToZone(PhysicalCard card, Zone zone, boolean end, boolean sharedMission) {
        if (zone == Zone.DISCARD &&
                getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            _inPlay.add(card);
        }

        if (zone.hasList()) {
            List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), zone);
            if (end)
                zoneCardList.add(card);
            else
                zoneCardList.addFirst(card);
        }

        if (card.getZone() != null)
            LOGGER.error("Card was in {} when tried to add to zone: {}", card.getZone(), zone);

        card.setZone(zone);
        for (GameStateListener listener : getAllGameStateListeners())
            sendCreatedCardToListener(card, sharedMission, listener,true);

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame(getGame());
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener,
                                             boolean animate) {
        sendCreatedCardToListener(card, sharedMission, listener, animate, false);
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener,
                                             boolean animate, boolean overrideOwnerVisibility) {
        GameEvent.Type eventType;

        if (sharedMission)
            eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
        else if (!animate)
            eventType = GameEvent.Type.PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING;
        else eventType = GameEvent.Type.PUT_CARD_INTO_PLAY;

        boolean sendGameEvent;
        if (card.getZone().isPublic())
            sendGameEvent = true;
        else if (card.getZone() == Zone.DISCARD && getGame().isDiscardPilePublic())
            sendGameEvent = true;
        else sendGameEvent = (overrideOwnerVisibility || card.getZone().isVisibleByOwner()) && card.getOwnerName().equals(listener.getPlayerId());

        if (sendGameEvent)
            listener.sendEvent(new GameEvent(eventType, card));
    }

    public void shuffleCardsIntoDeck(Iterable<? extends PhysicalCard> cards, String playerId) {

        for (PhysicalCard card : cards) {
            _cardGroups.get(Zone.DRAW_DECK).get(playerId).add(card);
            card.setZone(Zone.DRAW_DECK);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, true);
    }

    public void putCardOnTopOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, false);
    }

    public void iterateActiveCards(PhysicalCardVisitor physicalCardVisitor) {
        for (PhysicalCard physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard))
                if (physicalCardVisitor.visitPhysicalCard(physicalCard))
                    return;
        }

    }

    public PhysicalCard findCardById(int cardId) {
        return _allCards.get(cardId);
    }

    public Iterable<PhysicalCard> getAllCardsInGame() { return Collections.unmodifiableCollection(_allCards.values()); }
    public List<PhysicalCard> getAllCardsInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }
    public List<PhysicalCard> getHand(String playerId) { return getCardGroup(Zone.HAND, playerId); }

    public List<PhysicalCard> getRemoved(String playerId) { return getCardGroup(Zone.REMOVED, playerId); }
    public List<PhysicalCard> getDrawDeck(String playerId) { return getCardGroup(Zone.DRAW_DECK, playerId); }

    protected List<PhysicalCard> getCardGroup(Zone zone, String playerId) {
        return Collections.unmodifiableList(_cardGroups.get(zone).get(playerId));
    }
    public List<PhysicalCard> getDiscard(String playerId) { return getCardGroup(Zone.DISCARD, playerId); }

    public String getCurrentPlayerId() {
        return _playerOrder.getCurrentPlayer();
    }

    public void setCurrentPlayerId(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
    }

    public List<PhysicalCard> getAttachedCards(PhysicalCard card) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard physicalCard : _inPlay) {
            if (physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo() == card)
                result.add(physicalCard);
        }
        return result;
    }

    public void startPlayerTurn(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
        incrementCurrentTurnNumber();
        getAllGameStateListeners().forEach(listener -> listener.setCurrentPlayerId(playerId));
    }

    public boolean isCardInPlayActive(PhysicalCard card) {
        if (card.getAttachedTo() != null) return isCardInPlayActive(card.getAttachedTo());
        else return true;
    }

    public void startAffectingCardsForCurrentPlayer() {
        for (PhysicalCard physicalCard : _inPlay)
            if (isCardInPlayActive(physicalCard)) physicalCard.startAffectingGame(getGame());
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCard physicalCard : _inPlay)
            physicalCard.stopAffectingGame(getGame());
    }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPhase(phase);
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public PhysicalCard removeCardFromEndOfPile(String player, Zone zone, EndOfPile endOfPile) {
        List<PhysicalCard> deck = getZoneCards(player, zone);
        int pileIndex = endOfPile == EndOfPile.BOTTOM ? deck.size() - 1 : 0;
        if (!deck.isEmpty()) {
            final PhysicalCard removedCard = deck.get(pileIndex);
            removeCardsFromZone(null, Collections.singleton(removedCard));
            return removedCard;
        } else {
            return null;
        }
    }

    public void playerDrawsCard(String playerId) {
        List<PhysicalCard> deck = _cardGroups.get(Zone.DRAW_DECK).get(playerId);
        if (!deck.isEmpty()) {
            PhysicalCard card = deck.getFirst();
            removeCardsFromZone(playerId, Collections.singleton(card));
            addCardToZone(card, Zone.HAND);
        }
    }

    public void shuffleDeck(String playerId) {
        if (!getGame().getFormat().isNoShuffle())
            Collections.shuffle(_cardGroups.get(Zone.DRAW_DECK).get(playerId), ThreadLocalRandom.current());
    }

    public void sendGameStats() {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.GAME_STATS, this));
    }

    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public void addToPlayerScore(String playerId, int points) {
        int currentScore = _playerScores.get(playerId);
        _playerScores.put(playerId, currentScore + points);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerScore(playerId);
    }

    public int getPlayerScore(String playerId) {
        return _playerScores.get(playerId);
    }

    public Player getPlayer(String playerId) { return _players.get(playerId); }
    public Collection<Player> getPlayers() { return _players.values(); }

    public void discardHand(String playerId) {
        List<PhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (PhysicalCard card : hand) {
            addCardToZone(card, Zone.DISCARD);
        }
    }

    public Player getCurrentPlayer() { return getPlayer(getCurrentPlayerId()); }

    public void incrementCurrentTurnNumber() {
        _turnNumbers.put(getCurrentPlayerId(), _turnNumbers.get(getCurrentPlayerId())+1);
    }

    public int getPlayersLatestTurnNumber(String playerId) {
        return _turnNumbers.get(playerId);
    }

    public int getAndIncrementNextCardId() {
        int cardId = _nextCardId;
        _nextCardId++;
        return cardId;
    }


    public void updateGameStatsAndSendIfChanged() {
        boolean changed = false;

        Map<String, Map<Zone, Integer>> newZoneSizes = new HashMap<>();
        Map<String, Integer> newPlayerScores = new HashMap<>();

        if (_playerOrder != null) {
            for (String player : _playerOrder.getAllPlayers()) {
                final Map<Zone, Integer> playerZoneSizes = new EnumMap<>(Zone.class);
                playerZoneSizes.put(Zone.HAND, getHand(player).size());
                playerZoneSizes.put(Zone.DRAW_DECK, getDrawDeck(player).size());
                playerZoneSizes.put(Zone.DISCARD, getDiscard(player).size());
                playerZoneSizes.put(Zone.REMOVED, getRemoved(player).size());
                newZoneSizes.put(player, playerZoneSizes);
                newPlayerScores.put(player, getPlayerScore(player));
            }
        }

        if (!newZoneSizes.equals(_previousZoneSizes)) {
            changed = true;
            _previousZoneSizes = newZoneSizes;
        }

        if (!newPlayerScores.equals(_previousPlayerScores)) {
            changed = true;
            _previousPlayerScores = newPlayerScores;
        }

        if (changed) sendGameStats();
    }

    public Map<String, Map<Zone, Integer>> getZoneSizes() {
        return Collections.unmodifiableMap(_previousZoneSizes);
    }
    public Map<String, Integer> getPlayerScores() { return Collections.unmodifiableMap(_previousPlayerScores); }
    public ModifiersLogic getModifiersLogic() { return _modifiersLogic; }
    public void setModifiersLogic(ModifiersLogic modifiers) { _modifiersLogic = modifiers; }
    public void setNextCardId(int nextCardId) { _nextCardId = nextCardId; }

    public void setModifiersLogic(JsonNode node) { _modifiersLogic = new ModifiersLogic(getGame(), node); }
    public void setActionsEnvironment(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public PhysicalCard getCardFromCardId(int cardId) throws CardNotFoundException {
        PhysicalCard card = _allCards.get(cardId);
        if (card == null)
            throw new CardNotFoundException("Could not find card from id number " + cardId);
        return card;
    }

    public List<String> getMessages() { return getGame().getMessages(); }
    int getNextCardId() { return _nextCardId; }

    public void placeCardOnMission(PhysicalCard cardBeingPlaced, MissionLocation mission)
            throws InvalidGameLogicException {
        Zone currentZone = cardBeingPlaced.getZone();
        if (currentZone == null) {
            throw new InvalidGameLogicException("Tried to process a card not in any zone");
        } else {
            removeCardsFromZone(cardBeingPlaced.getOwnerName(), Arrays.asList(cardBeingPlaced));
            cardBeingPlaced.setPlacedOnMission(true);
            cardBeingPlaced.setLocation(mission);
            addCardToZone(cardBeingPlaced, Zone.AT_LOCATION);
            cardBeingPlaced.setLocation(mission);
        }
    }
}
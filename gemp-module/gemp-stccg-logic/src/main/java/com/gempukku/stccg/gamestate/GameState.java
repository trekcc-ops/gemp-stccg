package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.PlayCardState;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardVisitor;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.UserFeedback;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.modifiers.ModifierFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.gempukku.stccg.gamestate.GameEvent.Type.REMOVE_CARD_FROM_PLAY;

public abstract class GameState implements Snapshotable<GameState> {
    private static final Logger LOGGER = LogManager.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    protected PlayerOrder _playerOrder;
    protected final Map<Zone, Map<String, List<PhysicalCard>>> _cardGroups = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _stacked = new HashMap<>();
    protected final List<PhysicalCard> _inPlay = new LinkedList<>();

    protected final Map<Integer, PhysicalCard> _allCards = new HashMap<>();

    protected Phase _currentPhase;

    private boolean _consecutiveAction;

    protected final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    protected final LinkedList<String> _lastMessages = new LinkedList<>();
    protected final Map<String, CardDeck> _decks;
    private final Stack<PlayCardState> _playCardState = new Stack<>();

    protected int _nextCardId = 0;
    private final Map<String, Integer> _turnNumbers = new HashMap<>();

    public GameState(Map<String, CardDeck> decks) {
        _decks = decks;
        Collection<Zone> cardGroupList = new LinkedList<>();
        cardGroupList.add(Zone.DRAW_DECK);
        cardGroupList.add(Zone.HAND);
        cardGroupList.add(Zone.VOID);
        cardGroupList.add(Zone.VOID_FROM_HAND);
        cardGroupList.add(Zone.DISCARD);
        cardGroupList.add(Zone.REMOVED);

        cardGroupList.forEach(cardGroup -> _cardGroups.put(cardGroup, new HashMap<>()));
        for (String playerId : decks.keySet()) {
            cardGroupList.forEach(cardGroup -> _cardGroups.get(cardGroup).put(playerId, new LinkedList<>()));
            _turnNumbers.put(playerId, 0);
        }
    }

    public abstract DefaultGame getGame();

    public void init(PlayerOrder playerOrder, String firstPlayer) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(firstPlayer);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard();
        }
    }

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

    public PhysicalCard createPhysicalCard(String playerId, CardBlueprintLibrary library, String blueprintId)
            throws CardNotFoundException {
        CardBlueprint card = library.getCardBlueprint(blueprintId);

        int cardId = _nextCardId++;
        PhysicalCard result = new PhysicalCardGeneric(getGame(), cardId, playerId, card);

        _allCards.put(cardId, result);

        return result;
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

    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameStateListeners.add(gameStateListener);
        sendGameStateToClient(playerId, gameStateListener, false);
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    public void sendStateToAllListeners() {
        for (GameStateListener gameStateListener : _gameStateListeners)
            sendGameStateToClient(gameStateListener.getPlayerId(), gameStateListener, true);
    }

    public void sendGameStateToClient(String playerId, GameStateListener listener, boolean restoreSnapshot) {
        if (_playerOrder != null) {
            listener.initializeBoard();
            if (getCurrentPlayerId() != null) listener.setCurrentPlayerId(getCurrentPlayerId());
            if (_currentPhase != null) listener.setCurrentPhase(_currentPhase);

            sendCardsToClient(playerId, listener, restoreSnapshot);
        }
        for (String lastMessage : _lastMessages)
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

        List<PhysicalCard> cardsPutIntoPlay = new LinkedList<>();
        _stacked.values().forEach(cardsPutIntoPlay::addAll);
        cardsPutIntoPlay.addAll(_cardGroups.get(Zone.HAND).get(playerId));
        cardsPutIntoPlay.addAll(_cardGroups.get(Zone.DISCARD).get(playerId));
        for (PhysicalCard physicalCard : cardsPutIntoPlay) {
            sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
        }

        listener.sendEvent(new GameEvent(GameEvent.Type.GAME_STATS, getGame().getTurnProcedure().getGameStats()));
    }

    public void sendMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
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

    public void stackCard(PhysicalCard card, PhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        card.stackOn(stackOn);
        addCardToZone(card, Zone.STACKED);
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
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
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
            if (!zoneCards.contains(card))
                LOGGER.error("Card was not found in the expected zone");
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) card.stopAffectingGame();
            if (zone == Zone.STACKED || zone == Zone.DISCARD)
                if (card.isAffectingGame()) card.stopAffectingGameInZone(zone);

            getZoneCards(card.getOwnerName(), zone).remove(card);

            if (zone.isInPlay())
                _inPlay.remove(card);
            if (zone == Zone.ATTACHED)
                card.attachTo(null);

            if (zone == Zone.STACKED)
                card.stackOn(null);

            //If this is reset, then there is no way for self-discounting effects (which are evaluated while in the void)
            // to have any sort of permanent effect once the card is in play.
            if(zone != Zone.VOID_FROM_HAND && zone != Zone.VOID)
                card.setWhileInZoneData(null);
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
                listener.sendEvent(new GameEvent(REMOVE_CARD_FROM_PLAY, removedCardsVisibleByPlayer, getPlayer(playerPerforming)));
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
//            assignNewCardId(card); // Possibly it was a mistake commenting this out, but I'm pretty sure we should keep cardId permanent
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
            card.startAffectingGame();
        if ((zone == Zone.STACKED || zone == Zone.DISCARD) && card.isAffectingGame())
            card.startAffectingGameInZone(zone);
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener, boolean animate) {
        sendCreatedCardToListener(card, sharedMission, listener, animate, false);
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener, boolean animate, boolean overrideOwnerVisibility) {
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

    public void shuffleCardsIntoDeck(Collection<? extends PhysicalCard> cards, String playerId) {

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

    public Map<String, List<PhysicalCard>> getStackedCards() {
        return _stacked;
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
        // Active non-sites are affecting
        for (PhysicalCard physicalCard : _inPlay)
            if (isCardInPlayActive(physicalCard)) physicalCard.startAffectingGame();

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if ((isCardInPlayActive(stackedCard.getStackedOn()) && stackedCard.isAffectingGame()))
                    stackedCard.startAffectingGameInZone(Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _cardGroups.get(Zone.DISCARD).values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame())
                    discardedCard.startAffectingGameInZone(Zone.DISCARD);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCard physicalCard : _inPlay)
            physicalCard.stopAffectingGame();

        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    if (stackedCard.isAffectingGame()) stackedCard.stopAffectingGameInZone(Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _cardGroups.get(Zone.DISCARD).values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame()) discardedCard.stopAffectingGameInZone(Zone.DISCARD);
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
        Collections.shuffle(_cardGroups.get(Zone.DRAW_DECK).get(playerId), ThreadLocalRandom.current());
    }

    public void sendGameStats(GameStats gameStats) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.GAME_STATS, gameStats));
    }

    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public void addToPlayerScore(String playerId, int points) {
        Player player = getPlayer(playerId);
        player.scorePoints(points);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerScore(playerId);
    }

    public int getPlayerScore(String playerId) {
        Player player = getPlayer(playerId);
        return player.getScore();
    }

    public Player getPlayer(String playerId) { return getGame().getPlayerFromId(playerId); }
    public Collection<Player> getPlayers() { return getGame().getPlayers(); }

    public void discardHand(String playerId) {
        List<PhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (PhysicalCard card : hand) {
            addCardToZone(card, Zone.DISCARD);
        }
    }

    public Player getCurrentPlayer() { return getPlayer(getCurrentPlayerId()); }

    //
    // Play card state info
    //
    public void beginPlayCard(PlayCardAction action) {
            // TODO SNAPSHOT - Should be called at the beginning of every play card action
        int id = _playCardState.size();
        _playCardState.push(new PlayCardState(id, action));
    }

    /**
     * Gets the top play card state, or the 2nd to top play card state if the source card is the top.
     * @param sourceCardToSkip the sourceCard of the top play card state to skip, or null
     * @return the current top play card state, or null
     */
    public PlayCardState getTopPlayCardState(PhysicalCard sourceCardToSkip) {
            // TODO SNAPSHOT - Star Wars GEMP calls this function in filters and for a few blueprints
        if (_playCardState.isEmpty())
            return null;

        PlayCardState topPlayCardState = _playCardState.peek();
        if (sourceCardToSkip != null
                && topPlayCardState != null
                && topPlayCardState.getPlayCardAction().getCardEnteringPlay().getCardId() ==
                sourceCardToSkip.getCardId()) {
            int numPlayCardStates = _playCardState.size();
            return (numPlayCardStates > 1 ? _playCardState.subList(numPlayCardStates - 2, numPlayCardStates - 1).getFirst() : null);
        }
        return topPlayCardState;
    }

    /**
     * Gets all the play card states.
     * @return the play card states
     */
    public List<PlayCardState> getPlayCardStates() { // TODO SNAPSHOT - Should be called by ModifiersLogic
        if (_playCardState.isEmpty())
            return Collections.emptyList();

        return _playCardState.subList(0, _playCardState.size());
    }

    public void endPlayCard() { // TODO SNAPSHOT - Should be called at end of every PlayCardAction
            // TODO - Fairly sure this would be more appropriate as part of the Action, i.e. initiation, results, etc.
        PlayCardState state = getTopPlayCardState(null);
        if (state == null) {
            return;
        }
            // TODO SNAPSHOT - Review against Star Wars GEMP algorithm for PlayCardActions
/*        PlayCardAction action = state.getPlayCardAction();
        getGame().getModifiersEnvironment().removeEndOfCardPlayed(action.getPlayedCard());
        if (action.getOtherPlayedCard() != null) {
            getGame().getModifiersEnvironment().removeEndOfCardPlayed(action.getOtherPlayedCard());
        } */
        _playCardState.pop();
    }

    public void incrementCurrentTurnNumber() {
        _turnNumbers.put(getCurrentPlayerId(), _turnNumbers.get(getCurrentPlayerId())+1);
    }

    public int getPlayersLatestTurnNumber(String playerId) {
        return _turnNumbers.get(playerId);
    }

    @Override
    public void generateSnapshot(GameState selfSnapshot, SnapshotData snapshotData) {
            // TODO SNAPSHOT - Add content here
    }

    public int getAndIncrementNextCardId() {
        int cardId = _nextCardId;
        _nextCardId++;
        return cardId;
    }

}
package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardVisitor;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.processes.GameProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "currentPhase", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams" })
@JsonPropertyOrder({ "currentPhase", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams" })
public abstract class GameState {
    private static final Logger LOGGER = LogManager.getLogger(GameState.class);
    Phase _currentPhase;
    Map<String, Player> _players = new HashMap<>();
    PlayerOrder _playerOrder;
    protected final Map<Integer, PhysicalCard<DefaultGame>> _allCards = new HashMap<>();
    private ModifiersLogic _modifiersLogic;
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();
    int _nextCardId = 1;
    private ActionsEnvironment _actionsEnvironment;
    private GameProcess _currentGameProcess;

    protected GameState(DefaultGame game, Iterable<String> playerIds) {
        Collection<Zone> cardGroupList = new LinkedList<>();
        cardGroupList.add(Zone.DRAW_DECK);
        cardGroupList.add(Zone.HAND);
        cardGroupList.add(Zone.DISCARD);
        cardGroupList.add(Zone.REMOVED);

        try {
            for (String playerId : playerIds) {
                Player player = new Player(playerId);
                for (Zone zone : cardGroupList) {
                    player.addCardGroup(zone);
                }
                _players.put(playerId, player);
            }
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            game.cancelGame();
        }
        _modifiersLogic = new ModifiersLogic(game);
        _actionsEnvironment = new DefaultActionsEnvironment();
    }

    public void initializePlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(playerOrder.getFirstPlayer());
    }

    public void loadPlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
    }
    public ActionsEnvironment getActionsEnvironment() { return _actionsEnvironment; }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }

    public void sendCardsToClient(String playerId, GameStateListener listener, boolean restoreSnapshot)
            throws PlayerNotFoundException {

        Player player = getPlayer(playerId);
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

        cardsPutIntoPlay.addAll(player.getCardsInGroup(Zone.HAND));
        cardsPutIntoPlay.addAll(player.getCardsInGroup(Zone.DISCARD));
        for (PhysicalCard physicalCard : cardsPutIntoPlay) {
            sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
        }

        listener.sendEvent(new GameEvent(listener.getGame(), GameEvent.Type.GAME_STATS, this));
    }

    public void playerDecisionStarted(DefaultGame cardGame, String playerId, AwaitingDecision awaitingDecision)
            throws PlayerNotFoundException {
        _playerDecisions.put(playerId, awaitingDecision);
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            sendAwaitingDecisionToListener(listener, playerId, awaitingDecision);
    }

    public AwaitingDecision getDecision(String playerId) {
        return _playerDecisions.get(playerId);
    }

    public void sendAwaitingDecisionToListener(GameStateListener listener, String playerId, AwaitingDecision decision) throws PlayerNotFoundException {
        if (decision != null)
            listener.decisionRequired(playerId, decision);
    }

    public void playerDecisionFinished(String playerId, UserFeedback userFeedback) {
        userFeedback.removeDecision(playerId);
        _playerDecisions.remove(playerId);
    }

    public void transferCard(DefaultGame cardGame, PhysicalCard card, PhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            card.setZone(Zone.ATTACHED);

        card.attachTo(transferTo);
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            listener.sendEvent(new GameEvent(cardGame, GameEvent.Type.MOVE_CARD_IN_PLAY,card));
    }

    public void detachCard(DefaultGame cardGame, PhysicalCard attachedCard, Zone newZone) {
        attachedCard.setZone(newZone);
        attachedCard.detach();
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            listener.sendEvent(new GameEvent(cardGame, GameEvent.Type.MOVE_CARD_IN_PLAY,attachedCard));
    }


    public void attachCard(PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        card.attachTo(attachTo);
        addCardToZone(card, Zone.ATTACHED);
    }

    public List<PhysicalCard> getZoneCards(Player player, Zone zone) {
        List<PhysicalCard> zoneCards = player.getCardGroupCards(zone);
        return Objects.requireNonNullElse(zoneCards, _inPlay);
    }


    public void removeCardFromZone(PhysicalCard card) {
        removeCardsFromZone(card.getGame(), card.getOwnerName(), Collections.singleton(card));
    }

    public void moveCard(DefaultGame cardGame, PhysicalCard card) {
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            listener.sendEvent(new GameEvent(cardGame, GameEvent.Type.MOVE_CARD_IN_PLAY, card));
    }


    public void removeCardsFromZone(DefaultGame cardGame, String playerPerforming, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            List<PhysicalCard> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card) && card.getZone() != Zone.VOID)
                LOGGER.error(
                        "Card was not found in the expected zone: " + card.getTitle() + ", " + card.getZone().name());
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) card.stopAffectingGame(card.getGame());

            getZoneCards(card.getOwner(), zone).remove(card);

            if (card instanceof PhysicalReportableCard1E reportable) {
                if (reportable.getAwayTeam() != null) {
                    reportable.leaveAwayTeam((ST1EGame) cardGame);
                }
            }

            if (zone.isInPlay())
                _inPlay.remove(card);
            if (zone == Zone.ATTACHED)
                card.detach();
        }

        for (GameStateListener listener : cardGame.getAllGameStateListeners()) {

            Set<PhysicalCard> removedCardsVisibleByPlayer = new HashSet<>();
            for (PhysicalCard card : cards) {
                boolean publicDiscard = card.getZone() == Zone.DISCARD && listener.getGame().isDiscardPilePublic();
                if (card.getZone().isPublic() || publicDiscard ||
                        (card.getZone().isVisibleByOwner() && card.getOwnerName().equals(listener.getPlayerId())))
                    removedCardsVisibleByPlayer.add(card);
            }
            if (!removedCardsVisibleByPlayer.isEmpty()) {
                GameEvent event = new GameEvent(listener.getGame(), GameEvent.Type.REMOVE_CARD_FROM_PLAY,
                        removedCardsVisibleByPlayer, playerPerforming);
                listener.sendEvent(event);
            }
        }

        for (PhysicalCard card : cards) {
            card.setZone(Zone.VOID);
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
                getModifiersQuerying().hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            _inPlay.add(card);
        }

        if (zone.hasList()) {
            List<PhysicalCard> zoneCardList = getZoneCards(card.getOwner(), zone);
            if (end)
                zoneCardList.add(card);
            else
                zoneCardList.addFirst(card);
        }

        if (card.getZone() != null && card.getZone() != Zone.VOID)
            LOGGER.error("Card was in {} when tried to add to zone: {}", card.getZone(), zone);

        card.setZone(zone);
        for (GameStateListener listener : card.getGame().getAllGameStateListeners())
            sendCreatedCardToListener(card, sharedMission, listener,true);

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame(card.getGame());
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener,
                                             boolean animate) {
        sendCreatedCardToListener(card, sharedMission, listener, animate, false);
    }

    public void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener,
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
        else if (card.getZone() == Zone.DISCARD && listener.getGame().isDiscardPilePublic())
            sendGameEvent = true;
        else sendGameEvent = (overrideOwnerVisibility || card.getZone().isVisibleByOwner()) && card.getOwnerName().equals(listener.getPlayerId());

        if (sendGameEvent)
            listener.sendEvent(new GameEvent(card.getGame(), eventType, card));
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, true);
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

    @JsonProperty("cardsInGame")
    public Iterable<PhysicalCard> getAllCardsInGame() { return Collections.unmodifiableCollection(_allCards.values()); }
    public List<PhysicalCard> getAllCardsInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

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

    public void startPlayerTurn(DefaultGame cardGame, Player player) {
        _playerOrder.setCurrentPlayer(player.getPlayerId());
        player.incrementTurnNumber();
        cardGame.getAllGameStateListeners().forEach(listener -> listener.setCurrentPlayerId(player.getPlayerId()));
    }


    public boolean isCardInPlayActive(PhysicalCard card) {
        if (card.getAttachedTo() != null) return isCardInPlayActive(card.getAttachedTo());
        else return true;
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public void playerDrawsCard(DefaultGame game, Player player) {
        CardPile drawDeck = player.getDrawDeck();
        if (!drawDeck.isEmpty()) {
            PhysicalCard card = drawDeck.getTopCard();
            removeCardsFromZone(game, player.getPlayerId(), Collections.singleton(card));
            addCardToZone(card, Zone.HAND);
        }
    }


    public void sendGameStats(DefaultGame cardGame) {
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            listener.sendEvent(new GameEvent(cardGame, GameEvent.Type.GAME_STATS, this));
    }


    public Player getPlayer(String playerId) throws PlayerNotFoundException {
        Player player = _players.get(playerId);
        if (player != null) {
            return player;
        } else {
            throw new PlayerNotFoundException("Player " + playerId + " not found");
        }
    }
    public Collection<Player> getPlayers() { return _players.values(); }

    public Player getCurrentPlayer() throws PlayerNotFoundException {
        return getPlayer(getCurrentPlayerId());
    }

    public int getAndIncrementNextCardId() {
        int cardId = _nextCardId;
        _nextCardId++;
        return cardId;
    }


    public ModifiersLogic getModifiersLogic() { return _modifiersLogic; }
    public void setModifiersLogic(ModifiersLogic modifiers) { _modifiersLogic = modifiers; }
    public void setNextCardId(int nextCardId) { _nextCardId = nextCardId; }

    public void setModifiersLogic(JsonNode node, ST1EGame game) { _modifiersLogic = new ModifiersLogic(game, node); }
    public void setActionsEnvironment(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public PhysicalCard<DefaultGame> getCardFromCardId(int cardId) throws CardNotFoundException {
        PhysicalCard<DefaultGame> card = _allCards.get(cardId);
        if (card == null)
            throw new CardNotFoundException("Could not find card from id number " + cardId);
        return card;
    }

    int getNextCardId() { return _nextCardId; }

    public void placeCardOnMission(DefaultGame cardGame, PhysicalCard cardBeingPlaced, MissionLocation mission)
            throws InvalidGameLogicException {
        Zone currentZone = cardBeingPlaced.getZone();
        if (currentZone == null) {
            throw new InvalidGameLogicException("Tried to process a card not in any zone");
        } else {
            removeCardsFromZone(cardGame, cardBeingPlaced.getOwnerName(), List.of(cardBeingPlaced));
            cardBeingPlaced.setPlacedOnMission(true);
            cardBeingPlaced.setLocation(mission);
            addCardToZone(cardBeingPlaced, Zone.AT_LOCATION);
            cardBeingPlaced.setLocation(mission);
        }
    }


    public GameProcess getCurrentProcess() {
        return _currentGameProcess;
    }

    public void setCurrentProcess(GameProcess process) {
        _currentGameProcess = process;
    }

    public abstract void checkVictoryConditions(DefaultGame cardGame);

    protected ModifiersQuerying getModifiersQuerying() { return _modifiersLogic; }

    public void setCurrentPhaseNew(Phase phase) {
        _currentPhase = phase;
    }

    public void placeCardOnBottomOfDrawDeck(DefaultGame cardGame, Player owner, PhysicalCard card) {
        removeCardsFromZone(cardGame, owner.getPlayerId(), List.of(card));
        addCardToZone(card, Zone.DRAW_DECK, EndOfPile.BOTTOM);
    }

    public String serializeComplete() throws JsonProcessingException {
        return new GameStateMapper().writer(true).writeValueAsString(this);
    }

    public String serializeForPlayer(String playerId) throws JsonProcessingException {
        return new GameStateMapper().writer(false).writeValueAsString(
                new GameStateView(playerId, this));
    }


}
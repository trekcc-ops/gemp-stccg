package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
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
import com.gempukku.stccg.gameevent.*;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "currentPhase", "phasesInOrder", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams", "actions", "performedActions" })
@JsonPropertyOrder({ "currentPhase", "phasesInOrder", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams", "actions", "performedActions" })
public abstract class GameState {
    Phase _currentPhase;
    Map<String, Player> _players = new HashMap<>();
    PlayerOrder _playerOrder;
    protected final Map<Integer, PhysicalCard> _allCards = new HashMap<>();
    private ModifiersLogic _modifiersLogic;
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();
    int _nextCardId = 1;
    private ActionsEnvironment _actionsEnvironment;
    private GameProcess _currentGameProcess;
    private int _currentTurnNumber;

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

    public void sendCardsToClient(DefaultGame cardGame, String playerId, GameStateListener listener,
                                  boolean restoreSnapshot)
            throws PlayerNotFoundException, InvalidGameLogicException, InvalidGameOperationException {

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
        cardGame.sendActionResultToClient();
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

    public void sendAwaitingDecisionToListener(GameStateListener listener, String playerId, AwaitingDecision decision)
            throws PlayerNotFoundException {
        if (decision != null && listener.getPlayerId().equals(playerId))
            listener.decisionRequired(playerId, decision);
    }

    public void playerDecisionFinished(String playerId, UserFeedback userFeedback) {
        userFeedback.removeDecision(playerId);
        _playerDecisions.remove(playerId);
    }


    public void attachCard(PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");
        card.attachTo(attachTo);
        _inPlay.add(card);
        card.setZone(Zone.ATTACHED);

        for (GameStateListener listener : card.getGame().getAllGameStateListeners()) {
            try {
                sendCreatedCardToListener(card, false, listener, true);
            } catch(InvalidGameOperationException exp) {
                card.getGame().sendErrorMessage(exp);
            }
        }
        card.startAffectingGame(card.getGame());
    }

    public List<PhysicalCard> getZoneCards(Player player, Zone zone) {
        List<PhysicalCard> zoneCards = player.getCardGroupCards(zone);
        return Objects.requireNonNullElse(zoneCards, _inPlay);
    }


    public void removeCardFromZone(PhysicalCard card) {
        removeCardsFromZone(card.getGame(), card.getOwner(), Collections.singleton(card));
    }


    public void removeCardsFromZone(DefaultGame cardGame, Player performingPlayer, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            if (card.isInPlay()) card.stopAffectingGame(card.getGame());
            card.removeFromCardGroup();

            if (card instanceof PhysicalReportableCard1E reportable) {
                if (reportable.getAwayTeam() != null) {
                    reportable.leaveAwayTeam((ST1EGame) cardGame);
                }
            }

            if (card.isInPlay())
                _inPlay.remove(card);
            if (card.getAttachedTo() != null)
                card.detach();
        }

        for (GameStateListener listener : cardGame.getAllGameStateListeners()) {

            Set<PhysicalCard> removedCardsVisibleByPlayer = new HashSet<>();
            for (PhysicalCard card : cards) {
                if (card.isVisibleToPlayer(listener.getPlayerId()))
                    removedCardsVisibleByPlayer.add(card);
            }
            if (!removedCardsVisibleByPlayer.isEmpty()) {
                GameEvent event =
                        new RemoveCardsFromPlayGameEvent(removedCardsVisibleByPlayer, performingPlayer);
                listener.sendEvent(event);
            }
        }

        for (PhysicalCard card : cards) {
            card.setZone(Zone.VOID);
        }
    }

    public void removeCardsFromZoneWithoutSendingToClient(DefaultGame cardGame, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            if (card.isInPlay()) card.stopAffectingGame(card.getGame());
            card.removeFromCardGroup();

            if (card instanceof PhysicalReportableCard1E reportable) {
                if (reportable.getAwayTeam() != null) {
                    reportable.leaveAwayTeam((ST1EGame) cardGame);
                }
            }

            if (card.isInPlay())
                _inPlay.remove(card);
            if (card.getAttachedTo() != null)
                card.detach();
        }

        for (PhysicalCard card : cards) {
            card.setZone(Zone.VOID);
        }
    }




    public void addCardToZone(PhysicalCard card, Zone zone) {
        addCardToZone(card, zone, true, false);
    }

    public void addCardToZoneWithoutSendingToClient(PhysicalCard card, Zone zone) {
        if (zone == Zone.DISCARD &&
                getModifiersQuerying().hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            _inPlay.add(card);
        }

        if (zone.hasList()) {
            List<PhysicalCard> zoneCardList = getZoneCards(card.getOwner(), zone);
            zoneCardList.add(card);
        }

        card.setZone(zone);
        if (zone.isInPlay())
            card.startAffectingGame(card.getGame());
    }
    public void addCardToZone(PhysicalCard card, Zone zone, EndOfPile endOfPile) {
        addCardToZone(card, zone, endOfPile != EndOfPile.TOP, false);
    }

    public void addCardToTopOfDrawDeck(PhysicalCard card) {

        List<PhysicalCard> zoneCardList = getZoneCards(card.getOwner(), Zone.DRAW_DECK);
        zoneCardList.addFirst(card);

        card.setZone(Zone.DRAW_DECK);
        for (GameStateListener listener : card.getGame().getAllGameStateListeners()) {
            try {
                sendCreatedCardToListener(card, false, listener, true);
            } catch(InvalidGameOperationException exp) {
                card.getGame().sendErrorMessage(exp);
            }
        }
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, true, false);
    }


    public void addCardToBottomOfPlayPile(PhysicalCard card) {
        addCardToZone(card, Zone.PLAY_PILE, false, false);
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

        card.setZone(zone);
        for (GameStateListener listener : card.getGame().getAllGameStateListeners()) {
            try {
                sendCreatedCardToListener(card, sharedMission, listener, true);
            } catch(InvalidGameOperationException exp) {
                card.getGame().sendErrorMessage(exp);
            }
        }

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame(card.getGame());
    }

    protected void sendCreatedCardToListener(PhysicalCard card, boolean sharedMission, GameStateListener listener,
                                             boolean animate) throws InvalidGameOperationException {
        GameEvent.Type eventType;

        if (sharedMission)
            eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
        else if (!animate)
            eventType = GameEvent.Type.PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING;
        else eventType = GameEvent.Type.PUT_CARD_INTO_PLAY;

        if (card.isVisibleToPlayer(listener.getPlayerId())) {
            listener.sendEvent(new AddNewCardGameEvent(eventType, card));
        }
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
    private Map<Integer, PhysicalCard> getAllCardsForSerialization() {
        return _allCards;
    }

    @JsonIgnore
    public Iterable<PhysicalCard> getAllCardsInGame() {
        return Collections.unmodifiableCollection(_allCards.values());
    }
    public List<PhysicalCard> getAllCardsInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

    public String getCurrentPlayerId() {
        return _playerOrder.getCurrentPlayer();
    }

    public void setCurrentPlayerId(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
    }

    public void startPlayerTurn(DefaultGame cardGame, Player player) {
        _playerOrder.setCurrentPlayer(player.getPlayerId());
        _currentTurnNumber++;
        cardGame.getAllGameStateListeners().forEach(listener -> listener.setCurrentPlayerId(player.getPlayerId()));
    }


    public boolean isCardInPlayActive(PhysicalCard card) {
        if (card.getAttachedTo() != null) return isCardInPlayActive(card.getAttachedTo());
        else return true;
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public void playerDrawsCard(Player player) {
        CardPile drawDeck = player.getDrawDeck();
        if (!drawDeck.isEmpty()) {
            PhysicalCard card = drawDeck.getTopCard();
            drawDeck.remove(card);
            sendRemovedCardToListeners(card.getGame(), card);
            addCardToZone(card, Zone.HAND);
        }
    }

    private void sendRemovedCardToListeners(DefaultGame cardGame, PhysicalCard card) {
        for (GameStateListener listener : cardGame.getAllGameStateListeners()) {
            if (card.isVisibleToPlayer(listener.getPlayerId())) {
                GameEvent event =
                        new RemoveCardsFromPlayGameEvent(List.of(card), card.getOwner());
                listener.sendEvent(event);
            }
        }
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

    public void setModifiersLogic(ST1EGame game) { _modifiersLogic = new ModifiersLogic(game); }
    public void setActionsEnvironment(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public PhysicalCard getCardFromCardId(int cardId) throws CardNotFoundException {
        PhysicalCard card = _allCards.get(cardId);
        if (card == null)
            throw new CardNotFoundException("Could not find card from id number " + cardId);
        return card;
    }

    public void placeCardOnMission(DefaultGame cardGame, PhysicalCard cardBeingPlaced, MissionLocation mission) {
        removeCardsFromZone(cardGame, cardBeingPlaced.getOwner(), List.of(cardBeingPlaced));
        cardBeingPlaced.setPlacedOnMission(true);
        cardBeingPlaced.setLocation(mission);
        addCardToZone(cardBeingPlaced, Zone.AT_LOCATION);
        cardBeingPlaced.setLocation(mission);
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
        removeCardsFromZone(cardGame, owner, List.of(card));
        addCardToZone(card, Zone.DRAW_DECK, true, false);
    }

    public String serializeComplete() throws JsonProcessingException {
        return new GameStateMapper().writer(true).writeValueAsString(this);
    }

    public String serializeForPlayer(String playerId) throws JsonProcessingException {
        return new GameStateMapper().writer(false).writeValueAsString(
                new GameStateView(playerId, this));
    }

    @JsonProperty("actions")
    private Map<Integer, Action> getAllActions() {
        return _actionsEnvironment.getAllActions();
    }

    @JsonProperty("performedActions")
    @JsonIdentityReference(alwaysAsId=true)
    private List<Action> getPerformedActions() {
        return _actionsEnvironment.getPerformedActions();
    }


    @JsonProperty("phasesInOrder")
    abstract public List<Phase> getPhasesInOrder();

    public int getCurrentTurnNumber() {
        return _currentTurnNumber;
    }
}
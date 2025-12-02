package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "currentPhase", "phasesInOrder", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams", "actions", "performedActions", "playerClocks" })
@JsonPropertyOrder({ "currentPhase", "phasesInOrder", "currentProcess", "players", "playerOrder", "cardsInGame", "spacelineLocations",
        "awayTeams", "actions", "performedActions", "playerClocks" })
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
    private final Map<String, PlayerClock> _playerClocks;

    protected GameState(DefaultGame game, Iterable<String> playerIds, GameTimer gameTimer) {
        Collection<Zone> cardGroupList = new LinkedList<>();
        cardGroupList.add(Zone.DRAW_DECK);
        cardGroupList.add(Zone.HAND);
        cardGroupList.add(Zone.DISCARD);
        cardGroupList.add(Zone.REMOVED);

        _playerClocks = new HashMap<>();

        try {
            for (String playerId : playerIds) {
                Player player = new Player(playerId);
                for (Zone zone : cardGroupList) {
                    player.addCardGroup(zone);
                }
                _players.put(playerId, player);
                _playerClocks.put(playerId, new PlayerClock(playerId, gameTimer));
            }
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            game.cancelGame();
        }
        _modifiersLogic = new ModifiersLogic(game);
        _actionsEnvironment = new DefaultActionsEnvironment();
    }


    protected GameState(DefaultGame game, Iterable<String> playerIds, Map<String, PlayerClock> clocks) {
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
        _playerClocks = clocks;
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


    public void playerDecisionStarted(DefaultGame cardGame, String playerId, AwaitingDecision awaitingDecision)
            throws PlayerNotFoundException {
        if (awaitingDecision != null) {
            _playerDecisions.put(playerId, awaitingDecision);
            cardGame.sendActionResultToClient();
        }
    }

    public AwaitingDecision getDecision(String playerId) {
        return _playerDecisions.get(playerId);
    }

    public void playerDecisionFinished(String playerId, UserFeedback userFeedback) {
        userFeedback.removeDecision(playerId);
        _playerDecisions.remove(playerId);
    }


    public List<PhysicalCard> getZoneCards(Player player, Zone zone) {
        List<PhysicalCard> zoneCards = player.getCardGroupCards(zone);
        return Objects.requireNonNullElse(zoneCards, _inPlay);
    }

    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        try {
            Player player = getPlayer(playerId);
            List<PhysicalCard> zoneCards = player.getCardGroupCards(zone);
            return Objects.requireNonNullElse(zoneCards, _inPlay);
        } catch(PlayerNotFoundException exp) {
            return new ArrayList<>();
        }
    }

    public PhysicalCardGroup<? extends PhysicalCard> getCardGroup(String playerId, Zone zone) {
        try {
            Player player = getPlayer(playerId);
            return player.getCardGroup(zone);
        } catch(PlayerNotFoundException exp) {
            return null;
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


    public void addCardToZoneWithoutSendingToClient(PhysicalCard card, Zone zone) {
        if (zone == Zone.DISCARD &&
                getModifiersQuerying().hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            _inPlay.add(card);
        }

        if (zone.hasList()) {
            List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), zone);
            zoneCardList.add(card);
        }

        card.setZone(zone);
        if (zone.isInPlay())
            card.startAffectingGame(card.getGame());
    }
    public void addCardToTopOfDiscardOrDrawDeckWithoutSendingToClient(PhysicalCard card, Zone zone) {
        if (zone == Zone.DISCARD &&
                getModifiersQuerying().hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), zone);
        zoneCardList.addFirst(card);

        card.setZone(zone);
        if (zone.isInPlay())
            card.startAffectingGame(card.getGame());
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

    public void startPlayerTurn(Player player) {
        _playerOrder.setCurrentPlayer(player.getPlayerId());
        _currentTurnNumber++;
    }


    public boolean isCardInPlayActive(PhysicalCard card) {
        if (card.getAttachedTo() != null) return isCardInPlayActive(card.getAttachedTo());
        else return true;
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public void playerDrawsCard(Player player) {
        DrawDeck drawDeck = player.getDrawDeck();
        if (!drawDeck.isEmpty()) {
            PhysicalCard card = drawDeck.getTopCard();
            drawDeck.remove(card);
            List<PhysicalCard> zoneCardList = getZoneCards(player, Zone.HAND);
            zoneCardList.add(card);
            card.setZone(Zone.HAND);
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
        removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(cardBeingPlaced));
        cardBeingPlaced.setPlacedOnMission(true);
        cardBeingPlaced.setLocation(mission);
        addCardToZoneWithoutSendingToClient(cardBeingPlaced, Zone.AT_LOCATION);
        cardBeingPlaced.setLocation(mission);
    }


    public GameProcess getCurrentProcess() {
        return _currentGameProcess;
    }

    public void setCurrentProcess(GameProcess process) {
        _currentGameProcess = process;
    }

    public abstract void checkVictoryConditions(DefaultGame cardGame);

    public ModifiersQuerying getModifiersQuerying() { return _modifiersLogic; }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
    }

    public String serializeComplete() throws JsonProcessingException {
        return new GameStateMapper().writer(true).writeValueAsString(this);
    }

    public String serializeForPlayer(String playerId) throws JsonProcessingException {
        return new GameStateMapper().writer(false).writeValueAsString(
                new GameStateView(playerId, this));
    }

    @SuppressWarnings("unused")
    @JsonProperty("actions")
    private Map<Integer, Action> getAllActions() {
        return _actionsEnvironment.getAllActions();
    }

    @SuppressWarnings("unused")
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

    @JsonIgnore
    public Map<String, PlayerClock> getPlayerClocks() {
        return _playerClocks;
    }

    @SuppressWarnings("unused")
    @JsonProperty("playerClocks")
    private Collection<PlayerClock> playerClockList() {
        return _playerClocks.values();
    }

    public void addCardToInPlay(PhysicalCard card) {
        if (!_inPlay.contains(card)) {
            _inPlay.add(card);
            card.startAffectingGame(card.getGame());
        }
    }

    public void continueCurrentProcess(DefaultGame cardGame) throws PlayerNotFoundException, InvalidGameLogicException {
        _currentGameProcess.continueProcess(cardGame);
    }
}
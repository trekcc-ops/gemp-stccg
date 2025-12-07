package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.modifiers.LimitCounter;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.rules.generic.RuleSet;

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
    private final ModifiersLogic _modifiersLogic;
    private final ActionLimitCollection _actionLimitCollection = new ActionLimitCollection();
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();
    int _nextCardId = 1;
    private final ActionsEnvironment _actionsEnvironment;
    private GameProcess _currentGameProcess;
    @JsonProperty("turnNumber")
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
        _modifiersLogic = new ModifiersLogic();
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
        _modifiersLogic = new ModifiersLogic();
        _actionsEnvironment = new DefaultActionsEnvironment();
        _playerClocks = clocks;
    }

    protected GameState(List<Player> players, PlayerClock[] playerClocks) {
        _playerClocks = new HashMap<>();

        for (Player player : players) {
            _players.put(player.getPlayerId(), player);
        }

        for (PlayerClock clock : playerClocks) {
            _playerClocks.put(clock.getPlayerId(), clock);
        }

        _modifiersLogic = new ModifiersLogic();
        _actionsEnvironment = new DefaultActionsEnvironment();
    }



    public void initializePlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(playerOrder.getFirstPlayer());
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
            if (card.isInPlay()) {
                _modifiersLogic.removeWhileThisCardInPlayModifiers(card);
            }
            card.removeFromCardGroup(cardGame);

            if (card instanceof ReportableCard reportable && cardGame instanceof ST1EGame stGame) {
                stGame.getGameState().removeCardFromAwayTeam(stGame, reportable);
            }

            if (card.isInPlay())
                _inPlay.remove(card);
            if (card.getAttachedToCardId() != null)
                card.detach();
        }

        for (PhysicalCard card : cards) {
            card.setZone(Zone.VOID);
        }
    }

    public void addCardToRemovedPile(PhysicalCard card) {
        List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), Zone.REMOVED);
        zoneCardList.add(card);
        card.setZone(Zone.REMOVED);
    }

    public void addCardToZone(DefaultGame cardGame, PhysicalCard card, Zone zone, ActionContext context) {
        if (zone == Zone.DISCARD) {
            cardGame.addCardToTopOfDiscardPile(card);
        } else if (zone == Zone.REMOVED) {
            addCardToRemovedPile(card);
        }else {
            if (zone.isInPlay()) {
                addCardToInPlay(cardGame, card, context);
            }

            if (zone.hasList()) {
                List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), zone);
                zoneCardList.add(card);
            }

            card.setZone(zone);

        }
    }

    public void addCardToTopOfDrawDeck(PhysicalCard card) {
        List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), Zone.DRAW_DECK);
        zoneCardList.addFirst(card);
        card.setZone(Zone.DRAW_DECK);
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

    public PhysicalCard getCardFromCardId(int cardId) throws CardNotFoundException {
        PhysicalCard card = _allCards.get(cardId);
        if (card == null)
            throw new CardNotFoundException("Could not find card from id number " + cardId);
        return card;
    }


    public GameProcess getCurrentProcess() {
        return _currentGameProcess;
    }

    public void setCurrentProcess(GameProcess process) {
        _currentGameProcess = process;
    }

    public abstract void checkVictoryConditions(DefaultGame cardGame);

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

    public void addCardToInPlay(DefaultGame cardGame, PhysicalCard card, ActionContext context) {
        if (!_inPlay.contains(card)) {
            _inPlay.add(card);

            // Get "while in play" modifiers
            CardBlueprint blueprint = card.getBlueprint();
            List<Modifier> gameTextModifiers =
                    new LinkedList<>(blueprint.getGameTextWhileActiveInPlayModifiers(cardGame, card,
                            new ActionContext(card, card.getControllerName())));
            RuleSet<? extends DefaultGame> ruleSet = cardGame.getRules();
            List<Modifier> modifiersPerRules = ruleSet.getModifiersWhileCardIsInPlay(card);

            Collection<Modifier> whileInPlayModifiers = new ArrayList<>();
            whileInPlayModifiers.addAll(gameTextModifiers);
            whileInPlayModifiers.addAll(modifiersPerRules);

            _modifiersLogic.addWhileThisCardInPlayModifiers(whileInPlayModifiers, card);
        }
    }

    public void continueCurrentProcess(DefaultGame cardGame) throws PlayerNotFoundException, InvalidGameLogicException {
        _currentGameProcess.continueProcess(cardGame);
    }

    public abstract boolean cardsArePresentWithEachOther(PhysicalCard... cards);

    public LimitCounter getUntilEndOfGameLimitCounter(PhysicalCard card, String prefix) {
        return _actionLimitCollection.getUntilEndOfGameLimitCounter(card, prefix);
    }

    public LimitCounter getUntilEndOfTurnLimitCounter(ActionBlueprint actionBlueprint) {
        return _actionLimitCollection.getUntilEndOfTurnLimitCounter(actionBlueprint);
    }

    public void signalEndOfTurn() {
        _modifiersLogic.signalEndOfTurn();
        _actionsEnvironment.signalEndOfTurn();
        _actionLimitCollection.signalEndOfTurn();
    }

    public void signalStartOfTurn(DefaultGame cardGame, String currentPlayerName) {
        _modifiersLogic.signalStartOfTurn(currentPlayerName);
        _actionLimitCollection.signalStartOfTurn(currentPlayerName);
        // Unstop all "stopped" cards
        // TODO - Does not account for cards that can be stopped for multiple turns
        for (PhysicalCard card : cardGame.getAllCardsInPlay()) {
            if (card instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }
    }

    public int getNormalCardPlaysAvailable(String playerName) {
        return _actionLimitCollection.getNormalCardPlaysAvailable(playerName);
    }

    public void useNormalCardPlay(String playerName) {
        _actionLimitCollection.useNormalCardPlay(playerName);
    }

}
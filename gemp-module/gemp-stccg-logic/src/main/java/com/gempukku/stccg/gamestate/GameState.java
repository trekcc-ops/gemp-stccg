package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
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
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameOperationException;
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
@JsonIgnoreProperties(value = { "performedActions", "phasesInOrder" }, allowGetters = true)
@JsonIncludeProperties({ "currentPhase", "phasesInOrder", "currentProcess", "playerOrder", "cardsInGame", "playerMap", "spacelineLocations",
        "awayTeams", "actions", "performedActions", "playerClocks", "actionLimits", "modifiers", "gameLocations", "spacelineElements",
"versionNumber" })
@JsonPropertyOrder({ "currentPhase", "phasesInOrder", "currentProcess", "playerOrder", "cardsInGame", "playerMap", "spacelineLocations",
        "awayTeams", "actions", "performedActions", "playerClocks", "actionLimits", "modifiers", "gameLocations", "spacelineElements",
"versionNumber" })
public abstract class GameState {

    @JsonProperty("versionNumber")
    protected final String VERSION_NUMBER = "1.2.0";
    Phase _currentPhase;
    PlayerOrder _playerOrder;
    protected final Map<Integer, PhysicalCard> _allCards = new HashMap<>();
    protected final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    @JsonProperty("actionLimits")
    private final ActionLimitCollection _actionLimitCollection;
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    int _nextCardId = 1;
    private final ActionsEnvironment _actionsEnvironment = new ActionsEnvironment();
    private GameProcess _currentGameProcess;
    @JsonProperty("turnNumber")
    private int _currentTurnNumber;
    private final Map<String, PlayerClock> _playerClocks;
    List<Player> _players = new ArrayList<>();

    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();
    private int nextDecisionId = 1;


    protected GameState(Iterable<String> playerIds, GameTimer gameTimer)
            throws InvalidGameOperationException {
        _playerClocks = new HashMap<>();
        Collection<Zone> cardGroupList = List.of(Zone.DRAW_DECK, Zone.HAND, Zone.DISCARD, Zone.REMOVED);

        for (String playerId : playerIds) {
            Player player = new Player(playerId);
            for (Zone zone : cardGroupList) {
                player.addCardGroup(zone);
            }
            _players.add(player);
            _playerClocks.put(playerId, new PlayerClock(playerId, gameTimer));
        }
        _actionLimitCollection = new ActionLimitCollection();
    }


    protected GameState(Iterable<String> playerIds, Map<String, PlayerClock> clocks)
            throws InvalidGameOperationException {
        Collection<Zone> cardGroupList = List.of(Zone.DRAW_DECK, Zone.HAND, Zone.DISCARD, Zone.REMOVED);

        for (String playerId : playerIds) {
            Player player = new Player(playerId);
            for (Zone zone : cardGroupList) {
                player.addCardGroup(zone);
            }
            _players.add(player);
        }
        _actionLimitCollection = new ActionLimitCollection();
        _playerClocks = clocks;
    }

    protected GameState(List<Player> players, PlayerClock[] playerClocks, ActionLimitCollection actionLimitCollection) {
        _playerClocks = new HashMap<>();
        _players.addAll(players);

        for (PlayerClock clock : playerClocks) {
            _playerClocks.put(clock.getPlayerId(), clock);
        }

        _actionLimitCollection = actionLimitCollection;
    }



    public void initializePlayerOrder(PlayerOrder playerOrder) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(playerOrder.getFirstPlayer());
    }

    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }


    public AwaitingDecision getDecision(String playerId) {
        return _awaitingDecisionMap.get(playerId);
    }

    public void addPendingDecision(AwaitingDecision decision) {
        _awaitingDecisionMap.put(decision.getDecidingPlayerId(), decision);
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

            if (card.isInPlay()) {
                _inPlay.remove(card);
            }
            card.clearParentCardRelationship();
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

    public void addCardToZone(DefaultGame cardGame, PhysicalCard card, Zone zone, GameTextContext context) {
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
    public List<PhysicalCard> getAllCardsInGame() {
        return _allCards.values().stream().toList();
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

    public void startPlayerTurn(String playerName) {
        _playerOrder.setCurrentPlayer(playerName);
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
        for (Player player : _players) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        throw new PlayerNotFoundException("Player " + playerId + " not found");
    }

    @JsonIgnore
    public Collection<Player> getPlayers() { return _players; }

    public Player getCurrentPlayer() throws PlayerNotFoundException {
        return getPlayer(getCurrentPlayerId());
    }

    public int getAndIncrementNextCardId() {
        int cardId = _nextCardId;
        _nextCardId++;
        return cardId;
    }

    public int getAndIncrementNextDecisionId() {
        int decisionId = nextDecisionId;
        nextDecisionId++;
        return decisionId;
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
        return new GameStateMapper()
                .writer(false)
                .writeValueAsString(new GameStateView(playerId, this));
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

    public void addCardToInPlay(DefaultGame cardGame, PhysicalCard card, GameTextContext context) {
        if (!_inPlay.contains(card)) {
            _inPlay.add(card);

            // Get "while in play" modifiers
            CardBlueprint blueprint = card.getBlueprint();
            List<Modifier> gameTextModifiers =
                    new LinkedList<>(blueprint.getGameTextWhileActiveInPlayModifiers(cardGame, card,
                            new GameTextContext(card, card.getControllerName())));
            RuleSet<? extends DefaultGame> ruleSet = cardGame.getRules();
            List<Modifier> modifiersPerRules = ruleSet.getModifiersWhileCardIsInPlay(card);

            Collection<Modifier> whileInPlayModifiers = new ArrayList<>();
            whileInPlayModifiers.addAll(gameTextModifiers);
            whileInPlayModifiers.addAll(modifiersPerRules);

            _modifiersLogic.addWhileThisCardInPlayModifiers(whileInPlayModifiers, card);
        }
    }

    public void continueCurrentProcess(DefaultGame cardGame) throws InvalidGameOperationException {
        _currentGameProcess.continueProcess(cardGame);
    }

    public LimitCounter getUntilEndOfGameLimitCounter(String playerName, PhysicalCard card,
                                                      ActionBlueprint actionBlueprint) {
        return _actionLimitCollection.getUntilEndOfGameLimitCounter(playerName, card, actionBlueprint);
    }

    public LimitCounter getPerGamePerCopyLimitCounter(String playerName, PhysicalCard card,
                                                      ActionBlueprint actionBlueprint) {
        return _actionLimitCollection.getPerGamePerCopyLimitCounter(playerName, card, actionBlueprint);
    }


    public LimitCounter getUntilEndOfTurnLimitCounter(String playerName, PhysicalCard card,
                                                      ActionBlueprint actionBlueprint) {
        return _actionLimitCollection.getUntilEndOfTurnLimitCounter(playerName, card, actionBlueprint);
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

    @JsonProperty("modifiers")
    private List<Modifier> getAllModifiers() {
        return _modifiersLogic.getAllModifiers();
    }

    public void removeDecision(String playerName) {
        _awaitingDecisionMap.remove(playerName);
    }

    public boolean hasNoPendingDecisions() {
        return _awaitingDecisionMap.isEmpty();
    }

    public Set<String> getUsersPendingDecision() {
        return _awaitingDecisionMap.keySet();
    }

    @JsonProperty("playerMap")
    private Map<String, Player> getPlayerMap() {
        Map<String, Player> result = new HashMap<>();
        for (Player player : _players) {
            result.put(player.getPlayerId(), player);
        }
        return result;
    }

}
package com.gempukku.stccg.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gameevent.ActionResultGameEvent;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.*;

public abstract class DefaultGame implements ActionsQuerying, ModifiersQuerying, UserFeedback {
    private static final int LAST_MESSAGE_STORED_COUNT = 15;

    // Game parameters
    protected final GameFormat _format;
    protected final CardBlueprintLibrary _library;

    // IRL game mechanics
    protected final Set<String> _allPlayerIds;
    final List<String> _lastMessages = new LinkedList<>();

    private final Collection<Modifier> _modifierSkipSet = new HashSet<>();
    protected boolean _cancelled;
    protected boolean _finished;
    protected String _winnerPlayerId;
    protected final Map<String, String> _losers = new HashMap<>();
    // Game code infrastructure

    // Only has one listener, to send data back to CardGameMediator when the game is finished
    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();
    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    protected final GameType _gameType;

    protected DefaultGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library,
                       GameType gameType, GameResultListener listener) {
        _format = format;
        _library = library;
        _allPlayerIds = decks.keySet();
        _gameType = gameType;
        if (listener != null) {
            _gameResultListeners.add(listener);
        }
    }


    public DefaultGame getGame() {
        return this;
    }

    public abstract GameState getGameState();
    public abstract RuleSet<? extends DefaultGame> getRules();
    public boolean shouldAutoPass(Phase phase, String playerName) {
        return false;
    }

    public GameFormat getFormat() {
        return _format;
    }

    public Set<String> getPlayerIds() { return _allPlayerIds; }

    public Collection<Player> getPlayers() { return getGameState().getPlayers(); }

    public Collection<PhysicalCard> getAllCardsInPlay() { return getGameState().getAllCardsInPlay(); }

    public void addGameStateListener(GameStateListener listener) {
        _gameStateListeners.add(listener);
    }


    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void setCancelled(boolean cancelled) {
        _cancelled = cancelled;
    }

    public void setFinished(boolean finished) {
        _finished = finished;
    }


    public void setCurrentPhase(Phase phase) {
        getGameState().setCurrentPhase(phase);
        sendActionResultToClient();
    }



    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayerIds);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    protected void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;
        if (getGameState() != null)
            sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        assert getGameState() != null;

        for (GameResultListener gameResultListener : _gameResultListeners) {
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);
        }

        _finished = true;
    }

    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                sendMessage(playerId + " lost due to: " + reason);
                if (_losers.size() + 1 == _allPlayerIds.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayerIds);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.getFirst(), "Last remaining player in game");
                }
            }
        }
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    public ActionsEnvironment getActionsEnvironment() {
        return getGameState().getActionsEnvironment();
    }

    public ModifiersLogic getModifiersEnvironment() {
        return getGameState().getModifiersLogic();
    }

    public void startGame() {
        try {
            carryOutPendingActionsUntilDecisionNeeded();
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
        }
    }

    public void carryOutPendingActionsUntilDecisionNeeded() throws InvalidGameOperationException {
        if (!_cancelled) {
            int numSinceDecision = 0;
            ActionsEnvironment actionsEnvironment = getActionsEnvironment();

            while (isCarryingOutEffects()) {
                numSinceDecision++;
                actionsEnvironment.carryOutPendingActions(this);
                sendActionResultToClient();

                // Check if an unusually large number loops since user decision, which means game is probably in a loop
                if (numSinceDecision >= 5000)
                    breakExcessiveLoop(numSinceDecision);
            }
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) throws InvalidGameLogicException {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";

        Stack<Action> actionStack = getActionsEnvironment().getActionStack();
        errorMessage = errorMessage + " Action stack size: " + actionStack.size();
        for (Action action : actionStack) {
            errorMessage = errorMessage + "Action " + actionStack.indexOf(action) + 1 + ": " + action.getClass().getSimpleName();
        }
        throw new InvalidGameLogicException(errorMessage);
    }
    

    public Player getPlayer(int index) throws PlayerNotFoundException {
        return getPlayer(getPlayerId(index));
    }

    public String getPlayerId(int index) {
        return getAllPlayerIds()[index - 1];
    }

    public Player getPlayer(String playerId) throws PlayerNotFoundException {
        return getGameState().getPlayer(playerId);
    }

    public String[] getAllPlayerIds() {
        return _allPlayerIds.toArray(new String[0]);
    }

    public Player getCurrentPlayer() throws PlayerNotFoundException {
        GameState gameState = getGameState();
        return gameState.getCurrentPlayer();
    }

    public String getOpponent(String playerId) {
            return getAllPlayerIds()[0].equals(playerId) ?
                    getAllPlayerIds()[1] : getAllPlayerIds()[0];
    }

    public void sendMessage(String message) {
        addMessage(message);
        for (GameStateListener listener : _gameStateListeners)
            listener.sendMessageEvent(message);
    }

    public Phase getCurrentPhase() { return getGameState().getCurrentPhase(); }

    public String getCurrentPlayerId() { return getGameState().getCurrentPlayerId(); }

    public void addPendingDecision(AwaitingDecision decision) {
        getGameState().addPendingDecision(decision);
    }

    public String getStatus() {
        final Phase currentPhase = getCurrentPhase();
        String gameStatus;
        if (_cancelled)
            gameStatus = "Cancelled";
        else if (_finished)
            gameStatus = "Finished";
        else if (currentPhase.isSeedPhase())
            gameStatus = "Seeding";
        else gameStatus = "Playing";
        return gameStatus;
    }

    public boolean isCarryingOutEffects() {
        return getGameState().hasNoPendingDecisions() && _winnerPlayerId == null;
    }

    public PhysicalCard getCardFromCardId(int cardId) throws CardNotFoundException {
        return getGameState().getCardFromCardId(cardId);
    }

    public List<String> getMessages() { return _lastMessages; }

    public void addMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
    }

    public void sendErrorMessage(Exception exp) {
        sendErrorMessage(exp.getMessage());
    }

    public void sendErrorMessage(String message) {
        sendMessage("ERROR: " + message);
    }

    public Action getActionById(int actionId) {
        ActionsEnvironment environment = getActionsEnvironment();
        return environment.getActionById(actionId);
    }


    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : _gameStateListeners)
            listener.sendWarning(player, warning);
    }

    public void sendActionResultToClient() {
        for (GameStateListener listener : _gameStateListeners)
            listener.sendEvent(new ActionResultGameEvent(getGameState(), listener.getPlayerId()));
    }


    public GameType getGameType() {
        return _gameType;
    }

    public void continueCurrentProcess() throws InvalidGameOperationException {
        getGameState().continueCurrentProcess(this);
    }

    public boolean processUserDecision(String playerName, int decisionId, String answer) {
        AwaitingDecision awaitingDecision = getAwaitingDecision(playerName);

        if (awaitingDecision != null) {
            if (awaitingDecision.getDecisionId() == decisionId && !isFinished()) {
                try {
                    removeDecision(playerName);
                    awaitingDecision.decisionMade(answer);
                    return true;
                } catch (DecisionResultInvalidException exp) {
                        /* Participant provided wrong answer - send a warning message,
                        and ask again for the same decision */
                    sendWarning(playerName, exp.getWarningMessage());
                    sendAwaitingDecision(awaitingDecision);
                }
            }
        }
        return false;
    }

    public PhysicalCard createPhysicalCard(String blueprintId, int cardId, String playerId) throws CardNotFoundException {
        CardBlueprint blueprint = _library.get(blueprintId);
        if (blueprint != null) {
            return blueprint.createPhysicalCard(cardId, playerId);
        } else {
            throw new CardNotFoundException("Unable to find card matching blueprint id " + blueprintId);
        }
    }

    public void addCardToTopOfDiscardPile(PhysicalCard card) {
        String cardOwnerName = card.getOwnerName();
        Zone zone = hasFlagActive(ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD) ? Zone.REMOVED : Zone.DISCARD;
        List<PhysicalCard> zoneCardList = getGameState().getZoneCards(cardOwnerName, zone);
        zoneCardList.addFirst(card);
        card.setZone(zone);
    }


    public List<Modifier> getAllModifiersByEffect(ModifierEffect modifierEffect) {
        return Collections.unmodifiableList(getModifiersEnvironment().getAllModifiersByEffect(modifierEffect));
    }

    public List<Modifier> getAllModifiers() {
        return Collections.unmodifiableList(getModifiersEnvironment().getAllModifiers());
    }

    public void addToSkipSet(Modifier modifier) {
        _modifierSkipSet.add(modifier);
    }
    public void removeFromSkipSet(Modifier modifier) {
        _modifierSkipSet.remove(modifier);
    }
    public boolean modifierIsInSkipSet(Modifier modifier) {
        return _modifierSkipSet.contains(modifier);
    }

    public Collection<ActionProxy> getAllActionProxies() {
        return getActionsEnvironment().getAllActionProxies();
    }

    public Action getCurrentAction() {
        return getActionsEnvironment().getCurrentAction();
    }
    public void addActionToStack(Action action) { getActionsEnvironment().addActionToStack(action); }

    public void removeCardsFromZone(Collection<PhysicalCard> cards) {
        getGameState().removeCardsFromZoneWithoutSendingToClient(this, cards);
    }

    public String serializeCompleteGameState() throws JsonProcessingException {
        return getGameState().serializeComplete();
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    public ST1EGame get1EGame() throws InappropriateGameTypeException {
        if (this instanceof ST1EGame stGame) {
            return stGame;
        }
        throw new InappropriateGameTypeException("Attempted to convert a non-1E game into a ST1EGame object");
    }

    public boolean isPlayersTurn(String playerName) {
        return getGameState().getCurrentTurnNumber() > 0 &&
                Objects.equals(getCurrentPlayerId(), playerName);
    }
}
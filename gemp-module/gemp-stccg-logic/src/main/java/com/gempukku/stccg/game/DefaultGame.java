package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.UserFeedback;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.DefaultUserFeedback;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.modifiers.ModifiersEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.processes.TurnProcedure;

import java.util.*;

public abstract class DefaultGame {
    // Game parameters
    protected final GameFormat _format;
    protected final CardBlueprintLibrary _library;
    // IRL game mechanics
    protected final Set<String> _allPlayerIds;
    protected final Map<String, Player> _players = new HashMap<>();

    // Endgame operations
    protected final Set<String> _requestedCancel = new HashSet<>();
    protected boolean _cancelled;
    protected boolean _finished;
    protected String _winnerPlayerId;
    protected final Map<String, String> _losers = new HashMap<>();
    // Game code infrastructure
    protected final Set<GameResultListener> _gameResultListeners = new HashSet<>();
    protected final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();
    protected ModifiersLogic _modifiersLogic = new ModifiersLogic(this);
    protected ActionsEnvironment _actionsEnvironment;
    protected final UserFeedback _userFeedback;
    private final List<GameSnapshot> _snapshots = new LinkedList<>();
    protected GameSnapshot _snapshotToRestore;
    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private int _nextSnapshotId;
    private final static int NUM_PREV_TURN_SNAPSHOTS_TO_KEEPS = 1;

    public DefaultGame(GameFormat format, Map<String, CardDeck> decks,
                       final CardBlueprintLibrary library) {
        _format = format;
        _userFeedback = new DefaultUserFeedback(this);
        _library = library;

        _allPlayerIds = decks.keySet();
        for (String playerId : _allPlayerIds)
            _players.put(playerId, new Player(this, playerId));

        _actionsEnvironment = new DefaultActionsEnvironment(this, new Stack<>());
    }

    public abstract GameState getGameState();
    public boolean shouldAutoPass(Phase phase) {
        return false;
    }

    public GameFormat getFormat() {
        return _format;
    }

    public Set<String> getPlayerIds() { return _allPlayerIds; }
    public Player getPlayerFromId(String playerId) { return _players.get(playerId); }
    public Collection<Player> getPlayers() { return _players.values(); }
    public boolean isCancelled() { return _cancelled; }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameStateListeners.add(gameStateListener);
        getGameState().sendGameStateToClient(playerId, gameStateListener, false);
    }

    public Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    public void sendStateToAllListeners() {
        for (GameStateListener gameStateListener : _gameStateListeners)
            getGameState().sendGameStateToClient(gameStateListener.getPlayerId(), gameStateListener, true);
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayerIds.size())
            cancelGameRequested();
    }
    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (getGameState() != null)
                sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }
    
    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (getGameState() != null) {
                sendMessage(
                        "Game was cancelled due to an error, the error was logged and will be fixed soon.");
                sendMessage(
                        "Please post the replay game link and description of what happened on the tech support forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
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
        getGameState().finish();

        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);

        _finished = true;
    }

    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (getGameState() != null)
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

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }
    public CardBlueprintLibrary getBlueprintLibrary() {
        return _library;
    }

    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    public ModifiersEnvironment getModifiersEnvironment() {
        return _modifiersLogic;
    }

    public ModifiersQuerying getModifiersQuerying() {
        return _modifiersLogic;
    }

    public abstract TurnProcedure getTurnProcedure();

    public void startGame() {
        if (!_cancelled)
            getTurnProcedure().carryOutPendingActionsUntilDecisionNeeded();
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        if (!_cancelled) {
            getTurnProcedure().carryOutPendingActionsUntilDecisionNeeded();

            while (_snapshotToRestore != null) {
                restoreSnapshot();
                carryOutPendingActionsUntilDecisionNeeded();
            }
        }
    }

    public Player getPlayer(int index) { return getGameState().getPlayer(getAllPlayerIds()[index-1]); }

    public String[] getAllPlayerIds() {
        return _allPlayerIds.toArray(new String[0]);
    }

    public Player getCurrentPlayer() { return getGameState().getCurrentPlayer(); }

    public List<GameSnapshot> getSnapshots() {
        return Collections.unmodifiableList(_snapshots);
    }

    public String getOpponent(String playerId) {
            // TODO - Only works for 2-player games
        if (getAllPlayerIds().length != 2)
            throw new RuntimeException("Tried to call getOpponent function with more than 2 players");
        else {
            return getAllPlayerIds()[0].equals(playerId) ?
                    getAllPlayerIds()[1] : getAllPlayerIds()[0];
        }
    }

    public void requestRestoreSnapshot(int snapshotId) {
        if (_snapshotToRestore == null) {
            for (Iterator<GameSnapshot> iterator = _snapshots.iterator(); iterator.hasNext();) {
                GameSnapshot gameSnapshot = iterator.next();
                if (gameSnapshot.getId() == snapshotId) {
                    _snapshotToRestore = gameSnapshot;
                }
                // After snapshot to restore is found, remove any snapshots after it from list
                if (_snapshotToRestore != null) {
                    // Remove the current snapshot from the iterator and the list.
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Determines if a snapshot is pending to be restored.
     * @return true or false
     */
    public boolean isRestoreSnapshotPending() {
        return _snapshotToRestore != null;
    }

    /**
     * Restores the snapshot as the current state of the game.
     */
    protected abstract void restoreSnapshot();

    /**
     * Creates a snapshot of the current state of the game.
     * @param description the description
     */
    public void takeSnapshot(String description) {
        // TODO - Star Wars code used PlayCardStates here
        pruneSnapshots();
        // need to specifically exclude when getPlayCardStates() is not empty to allow for battles to be initiated by interrupts
        _snapshots.add(GameSnapshot.createGameSnapshot(++_nextSnapshotId, description, getGameState(),
                _modifiersLogic, _actionsEnvironment, getTurnProcedure()));
    }

    /**
     * Prunes older snapshots.
     */
    private void pruneSnapshots() {
        // Remove old snapshots until reaching snapshots to keep
        for (Iterator<GameSnapshot> iterator = _snapshots.iterator(); iterator.hasNext();) {
            GameSnapshot gameSnapshot = iterator.next();
            String snapshotCurrentPlayer = gameSnapshot.getCurrentPlayerId();
            int snapshotCurrentTurnNumber = gameSnapshot.getCurrentTurnNumber();
            if (snapshotCurrentTurnNumber <= 1 &&
                    getGameState().getPlayersLatestTurnNumber(snapshotCurrentPlayer) <= 1) {
                break;
            }
            int pruneOlderThanTurn = getGameState().getPlayersLatestTurnNumber(snapshotCurrentPlayer) -
                    (NUM_PREV_TURN_SNAPSHOTS_TO_KEEPS / 2);
            if (snapshotCurrentTurnNumber >= pruneOlderThanTurn) {
                break;
            }
            // Remove the current snapshot from the iterator and the list.
            iterator.remove();
        }
    }
    
    public void sendMessage(String message) { getGameState().sendMessage(message); }
    public Phase getCurrentPhase() { return getGameState().getCurrentPhase(); }
    public String getCurrentPhaseString() { return getGameState().getCurrentPhase().getHumanReadable(); }
    public String getCurrentPlayerId() { return getGameState().getCurrentPlayerId(); }

    public AwaitingDecision getAwaitingDecision(String playerName) {
        return _userFeedback.getAwaitingDecision(playerName);
    }

    public Set<String> getUsersPendingDecision() {
        return _userFeedback.getUsersPendingDecision();
    }

    public void sendAwaitingDecision(String playerName, AwaitingDecision awaitingDecision) {
        _userFeedback.sendAwaitingDecision(playerName, awaitingDecision);
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

    public boolean isDiscardPilePublic() {
        return _format.discardPileIsPublic();
    }

}
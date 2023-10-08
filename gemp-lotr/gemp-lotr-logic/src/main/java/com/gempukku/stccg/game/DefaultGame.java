package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.modifiers.ModifiersEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.WinConditionRule;

import java.util.*;

public abstract class DefaultGame {
    // Game parameters
    protected GameFormat _format;
    protected CardBlueprintLibrary _library;
    // IRL game mechanics
    protected Set<String> _allPlayers;
    // Endgame operations
    protected final Set<String> _requestedCancel = new HashSet<>();
    protected boolean _cancelled;
    protected boolean _finished;
    protected String _winnerPlayerId;
    protected final Map<String, String> _losers = new HashMap<>();
    // Game code infrastructure
    protected final Set<GameResultListener> _gameResultListeners = new HashSet<>();
    protected final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();
    protected final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    protected DefaultActionsEnvironment _actionsEnvironment;
    protected UserFeedback _userFeedback;

    public DefaultGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {

        _format = format;
        _userFeedback = userFeedback;
        _library = library;

        _allPlayers = decks.keySet();

        _actionsEnvironment = new DefaultActionsEnvironment(this, new Stack<>());
        new WinConditionRule(_actionsEnvironment).applyRule();
    }

    public abstract GameState getGameState();
    public boolean shouldAutoPass(String playerId, Phase phase) {
        return false;
    }

    public boolean isSolo() {
        return _allPlayers.size() == 1;
    }

    public GameFormat getFormat() {
        return _format;
    }
    public abstract boolean checkPlayRequirements(PhysicalCard card);
    public Set<String> getPlayers() { return _allPlayers; }
    public boolean isCancelled() { return _cancelled; }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener, getTurnProcedure().getGameStats());
    }
    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayers.size())
            cancelGameRequested();
    }
    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (getGameState() != null)
                getGameState().sendMessage("Game was cancelled, as requested by all parties.");

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
                getGameState().sendMessage("Game was cancelled due to an error, the error was logged and will be fixed soon.");
                getGameState().sendMessage("Please post the replay game link and description of what happened on the TLHH forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayers);
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
            getGameState().sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

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
                    getGameState().sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayers.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayers);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.get(0), "Last remaining player in game");
                }
            }
        }
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        getGameState().removeGameStateListener(gameStateListener);
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
        if (!_cancelled)
            getTurnProcedure().carryOutPendingActionsUntilDecisionNeeded();
    }

    public CardBlueprintLibrary getCardBlueprintLibrary() { return _library; }
}

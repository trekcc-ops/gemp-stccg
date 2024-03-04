package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.*;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private ST2EGameState _gameState;
    private TurnProcedure _turnProcedure;
    private final ST2EGame _thisGame;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);
        _thisGame = this;

        _gameState = new ST2EGameState(_allPlayerIds, decks, library, _format, this);
        new RuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _turnProcedure = new TurnProcedure(this, userFeedback, _actionsEnvironment
        ) {
            @Override
            protected GameProcess setFirstGameProcess() {
                return _thisGame.getFormat().getStartingGameProcess(_allPlayerIds, _gameState::init, _thisGame);
            }
        };
    }


    @Override
    public ST2EGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

    protected void restoreSnapshot() {
        if (_snapshotToRestore != null) {
            if (!(_snapshotToRestore.getGameState() instanceof ST2EGameState))
                throw new RuntimeException("Tried to restore a snapshot with an invalid gamestate");
            else {
                _gameState = (ST2EGameState) _snapshotToRestore.getGameState();
                _modifiersLogic = _snapshotToRestore.getModifiersLogic();
                _actionsEnvironment = _snapshotToRestore.getActionsEnvironment();
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                getGameState().sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                getGameState().sendStateToAllListeners();
            }
        }
    }

    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener);
    }
}

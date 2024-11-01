package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.ST2EGameState;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.processes.st1e.ST1EGameProcess;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private ST2EGameState _gameState;
    private TurnProcedure _turnProcedure;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new ST2EGameState(decks, this);
        new RuleSet(this).applyRuleSet();

        _turnProcedure = new TurnProcedure(this, _userFeedback
        ) {
            @Override
            protected ST1EGameProcess setFirstGameProcess() {
                return null; // TODO - Needs to be replaced by a starting process for 2E
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
                throw new RuntimeException("Tried to restore a snapshot with an invalid game state");
            else {
                _gameState = (ST2EGameState) _snapshotToRestore.getGameState();
                _modifiersLogic = _snapshotToRestore.getModifiersLogic();
                _actionsEnvironment = _snapshotToRestore.getActionsEnvironment();
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                getGameState().sendStateToAllListeners();
            }
        }
    }

    @Override
    public void addGameStateListener(String playerId, GameStateListener listener) {
        getGameState().addGameStateListener(playerId, listener);
    }
}
package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private TribblesGameState _gameState;
    private TurnProcedure _turnProcedure;
    private final TribblesGame _thisGame;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);
        _thisGame = this;

        _gameState = new TribblesGameState(_allPlayerIds, decks, library, _format, this);
        new TribblesRuleSet(this).applyRuleSet();

        _gameState.createPhysicalCards();
        _turnProcedure = new TurnProcedure(this, _userFeedback
        ) {
            @Override
            protected GameProcess setFirstGameProcess() {
                return new TribblesPlayerOrderProcess(decks, _library, _gameState::init, _thisGame);
            }
        };
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

    protected void restoreSnapshot() {
        if (_snapshotToRestore != null) {
            if (!(_snapshotToRestore.getGameState() instanceof TribblesGameState))
                throw new RuntimeException("Tried to restore a snapshot with an invalid gamestate");
            else {
                _gameState = (TribblesGameState) _snapshotToRestore.getGameState();
                _modifiersLogic = _snapshotToRestore.getModifiersLogic();
                _actionsEnvironment = _snapshotToRestore.getActionsEnvironment();
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                getGameState().sendStateToAllListeners();
            }
        }
    }

}
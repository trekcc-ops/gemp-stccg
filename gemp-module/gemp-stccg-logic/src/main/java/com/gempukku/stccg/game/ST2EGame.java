package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ST2EGameState;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private ST2EGameState _gameState;
    private TurnProcedure _turnProcedure;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new ST2EGameState(decks.keySet(), this);
        new RuleSet(this).applyRuleSet();

        _turnProcedure = new TurnProcedure(this, null);
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
                _gameState.setModifiersLogic(_snapshotToRestore.getModifiersLogic());
                _gameState.setActionsEnvironment(_snapshotToRestore.getActionsEnvironment());
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                sendStateToAllListeners();
            }
        }
    }
}
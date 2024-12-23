package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerOrderProcess;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private TribblesGameState _gameState;
    private TurnProcedure _turnProcedure;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new TribblesGameState(decks.keySet(), this);
        new TribblesRuleSet(this).applyRuleSet();

        _gameState.createPhysicalCards(library, decks);
        _turnProcedure = new TurnProcedure(this, new TribblesPlayerOrderProcess(this));
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
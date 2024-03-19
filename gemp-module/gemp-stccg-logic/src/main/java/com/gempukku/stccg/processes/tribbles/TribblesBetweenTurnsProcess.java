package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.GameProcess;

public class TribblesBetweenTurnsProcess extends GameProcess {

    private final TribblesGame _game;
    public TribblesBetweenTurnsProcess(TribblesGame game) {
        super();
        _game = game;
    }
    @Override
    public void process() {
        TribblesGameState gameState = _game.getGameState();
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        ActionOrder actionOrder = gameState.getPlayerOrder().getStandardPlayOrder(gameState.getCurrentPlayerId(), false);

        actionOrder.getNextPlayer(); // TODO: This call is necessary but not logical
        String currentPlayer = actionOrder.getNextPlayer();

        while (_game.getGameState().getPlayerDecked(currentPlayer)) {
            _game.sendMessage(currentPlayer + " is decked. Skipping their turn.");
            actionOrder = gameState.getPlayerOrder().getStandardPlayOrder(currentPlayer, false);
            currentPlayer = actionOrder.getNextPlayer();
        }

        _game.getGameState().startPlayerTurn(currentPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesStartOfTurnGameProcess(_game);
    }
}

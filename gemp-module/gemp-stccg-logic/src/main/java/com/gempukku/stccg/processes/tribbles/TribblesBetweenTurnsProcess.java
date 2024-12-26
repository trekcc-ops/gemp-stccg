package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

public class TribblesBetweenTurnsProcess extends TribblesGameProcess {
    public TribblesBetweenTurnsProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process(DefaultGame cardGame) {
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
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new StartOfTurnGameProcess();
    }
}
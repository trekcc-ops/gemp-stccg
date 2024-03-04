package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;

public class BetweenTurnsProcess extends GameProcess {

    private final DefaultGame _game;
    private final GameProcess _nextProcess;
    public BetweenTurnsProcess(DefaultGame game, GameProcess nextProcess) {
        _game = game;
        _nextProcess = nextProcess;
    }
    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        _game.getGameState().sendMessage("DEBUG: Beginning BetweenTurnsProcess");
        ActionOrder actionOrder = _game.getGameState().getPlayerOrder().getClockwisePlayOrder(_game.getGameState().getCurrentPlayerId(), false);
        actionOrder.getNextPlayer();

        String nextPlayer = actionOrder.getNextPlayer();
        _game.getGameState().startPlayerTurn(nextPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}

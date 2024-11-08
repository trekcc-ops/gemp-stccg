package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.st1e.ST1EGameProcess;
import com.gempukku.stccg.processes.st1e.ST1EStartOfTurnGameProcess;

import java.util.HashSet;

public class ST1EBetweenTurnsProcess extends ST1EGameProcess {
    public ST1EBetweenTurnsProcess(ST1EGame game) {
        super(new HashSet<>(), game);
    }
    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        String playerId = _game.getGameState().getCurrentPlayerId();
        ActionOrder actionOrder = _game.getGameState().getPlayerOrder().getClockwisePlayOrder(playerId, false);
        actionOrder.getNextPlayer();

        String nextPlayer = actionOrder.getNextPlayer();
        _game.getGameState().startPlayerTurn(nextPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EStartOfTurnGameProcess(_game);
    }
}
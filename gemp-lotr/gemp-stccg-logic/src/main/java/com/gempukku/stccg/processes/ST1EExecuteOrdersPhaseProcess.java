package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EExecuteOrdersPhaseProcess implements GameProcess<ST1EGame> {
    private String _playerId;

    ST1EExecuteOrdersPhaseProcess(String playerId) {
        _playerId = playerId;
    }
    @Override
    public void process(ST1EGame game) {
        game.getGameState().sendMessage("DEBUG: Execute orders phase. Currently nothing is coded for this phase.");
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EEndOfTurnProcess(_playerId);
    }
}

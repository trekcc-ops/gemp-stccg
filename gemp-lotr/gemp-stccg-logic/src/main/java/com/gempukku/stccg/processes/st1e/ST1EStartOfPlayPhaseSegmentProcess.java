package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.turn.StartOfPhaseAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EStartOfPlayPhaseSegmentProcess extends GameProcess {
    private final Phase _phase;
    private final String _playerId;
    private final ST1EGame _game;

    public ST1EStartOfPlayPhaseSegmentProcess(Phase phase, ST1EGame game) {
        _phase = phase;
        _playerId = game.getCurrentPlayerId();
        _game = game;
    }

    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(_phase);
        _game.getActionsEnvironment().addActionToStack(new StartOfPhaseAction(_game, _phase));
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EPlayPhaseSegmentProcess(_game);
    }
}

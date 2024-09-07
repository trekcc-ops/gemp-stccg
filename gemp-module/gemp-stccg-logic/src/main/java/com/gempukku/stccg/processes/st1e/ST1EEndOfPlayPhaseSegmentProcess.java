package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.turn.EndOfPhaseAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EEndOfPlayPhaseSegmentProcess extends ST1EGameProcess {
    private final GameProcess _followingGameProcess;

    public ST1EEndOfPlayPhaseSegmentProcess(ST1EGame game) {
        super(game);
        Phase currentPhase = game.getGameState().getCurrentPhase();
        if (currentPhase == Phase.CARD_PLAY) {
            _followingGameProcess = new ST1EStartOfPlayPhaseSegmentProcess(Phase.EXECUTE_ORDERS, _game);
        } else if (currentPhase == Phase.EXECUTE_ORDERS) {
            _followingGameProcess = new ST1EEndOfTurnProcess(_game);
        } else throw new RuntimeException(
                "Constructed end of play phase segment process without being in a valid play phase segment");
    }


    @Override
    public void process() {
        _game.getActionsEnvironment().addActionToStack(new EndOfPhaseAction(_game));
    }

    @Override
    public GameProcess getNextProcess() { return _followingGameProcess; }
}
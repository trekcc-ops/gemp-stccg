package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.HashSet;

public class ST1EEndOfPlayPhaseSegmentProcess extends ST1EGameProcess {
    public ST1EEndOfPlayPhaseSegmentProcess(ST1EGame game) {
        super(new HashSet<>(), game);
    }

    @Override
    public void process() {
        String phaseString = _game.getCurrentPhaseString();
        String message = "End of " + phaseString + " phase";
        _game.sendMessage(message);
    }

    @Override
    public GameProcess getNextProcess() {
        Phase currentPhase = _game.getCurrentPhase();
        if (currentPhase == Phase.CARD_PLAY) {
            _game.getGameState().setCurrentPhase(Phase.EXECUTE_ORDERS);
            return new ST1EStartOfPlayPhaseSegmentProcess(_game);
        } else if (currentPhase == Phase.EXECUTE_ORDERS) {
            return new ST1EEndOfTurnProcess(_game);
        } else throw new RuntimeException(
                "Constructed end of play phase segment process without being in a valid play phase segment");
    }
}
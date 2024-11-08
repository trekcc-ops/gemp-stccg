package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.HashSet;

public class ST1EStartOfPlayPhaseSegmentProcess extends ST1EGameProcess {
    public ST1EStartOfPlayPhaseSegmentProcess(ST1EGame game) {
        super(new HashSet<>(), game);
    }

    @Override
    public void process() {
        Phase phase = _game.getCurrentPhase();
        String message = "Start of " + phase + " phase";
        _game.sendMessage("\n" + message);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EPlayPhaseSegmentProcess(_game);
    }
}
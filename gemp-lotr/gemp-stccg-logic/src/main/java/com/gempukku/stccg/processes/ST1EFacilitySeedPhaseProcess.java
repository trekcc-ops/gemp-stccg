package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1EFacilitySeedPhaseProcess implements GameProcess<ST1EGame> {

    public ST1EFacilitySeedPhaseProcess(int consecutivePasses, GameProcess<ST1EGame> followingGameProcess) {
    }

    @Override
    public void process(ST1EGame game) {
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() { return null;
    }
}
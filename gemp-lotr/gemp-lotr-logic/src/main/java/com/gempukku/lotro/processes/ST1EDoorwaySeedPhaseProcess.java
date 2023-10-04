package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.ST1EGame;

public class ST1EDoorwaySeedPhaseProcess implements GameProcess<ST1EGame> {

    public ST1EDoorwaySeedPhaseProcess() {
        // TODO: Build code for the doorway phase
    }

    @Override
    public void process(ST1EGame game) {
        // TODO: Build code for doorway phase
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
            // TODO - This will just loop indefinitely after the mission seed phase
        return new ST1EMissionSeedPhaseProcess(0, new ST1EDoorwaySeedPhaseProcess());
    }
}
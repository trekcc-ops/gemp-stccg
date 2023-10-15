package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;

public class ST1EDilemmaSeedPhaseProcess implements GameProcess<ST1EGame> {
    public ST1EDilemmaSeedPhaseProcess() {
    }

    @Override
    public void process(ST1EGame game) {
        game.getGameState().setCurrentPhase(Phase.SEED_DILEMMA);
        // TODO - Obviously everything for the dilemma seed phase process
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EStartOfFacilitySeedPhaseProcess();
    }
}
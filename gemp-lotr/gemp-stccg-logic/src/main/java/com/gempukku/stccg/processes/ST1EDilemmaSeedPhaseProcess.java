package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;

public class ST1EDilemmaSeedPhaseProcess extends ST1EGameProcess {
    public ST1EDilemmaSeedPhaseProcess(ST1EGame game) { super(game); }

    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(Phase.SEED_DILEMMA);
        // TODO - Obviously everything for the dilemma seed phase process
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EStartOfFacilitySeedPhaseProcess(_game);
    }
}
package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class PlayPhaseRequirement implements Requirement {

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        Phase currentPhase = cardGame.getCurrentPhase();
        return currentPhase != null && !currentPhase.isSeedPhase();
    }
}
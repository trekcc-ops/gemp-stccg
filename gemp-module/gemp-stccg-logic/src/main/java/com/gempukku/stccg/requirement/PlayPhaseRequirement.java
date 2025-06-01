package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Phase;

public class PlayPhaseRequirement implements Requirement {

    public boolean accepts(ActionContext actionContext) {
        Phase currentPhase = actionContext.getGame().getCurrentPhase();
        return currentPhase != null && !currentPhase.isSeedPhase();
    }
}
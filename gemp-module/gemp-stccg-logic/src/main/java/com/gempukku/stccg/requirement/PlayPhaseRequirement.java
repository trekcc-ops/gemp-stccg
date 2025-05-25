package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.evaluator.ValueSource;

public class PlayPhaseRequirement implements Requirement {

    public boolean accepts(ActionContext actionContext) {
        Phase currentPhase = actionContext.getGame().getCurrentPhase();
        return currentPhase != null && !currentPhase.isSeedPhase();
    }
}
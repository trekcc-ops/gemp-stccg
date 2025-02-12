package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Phase;

public class StartOfPhaseTriggerChecker implements TriggerChecker {

    private final Phase _phase;

    StartOfPhaseTriggerChecker(@JsonProperty(value = "phase", required = true)
                 Phase phase
    ) {
        _phase = phase;
    }

    @Override
    public boolean accepts(ActionContext actionContext) {
        return actionContext.hasActionResultType(ActionResult.Type.START_OF_PHASE) &&
                actionContext.getGameState().getCurrentPhase() == _phase;
    }

}
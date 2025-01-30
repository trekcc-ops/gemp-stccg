package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;

public class StartOfTurnTriggerChecker implements TriggerChecker {
        @Override
        public boolean accepts(ActionContext actionContext) {
            return actionContext.hasActionResultType(ActionResult.Type.START_OF_TURN);
        }

}
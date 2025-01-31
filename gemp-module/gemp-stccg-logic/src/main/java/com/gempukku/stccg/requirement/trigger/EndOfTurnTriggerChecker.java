package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;

public class EndOfTurnTriggerChecker implements TriggerChecker {

        @Override
        public boolean accepts(ActionContext actionContext) {
            return actionContext.hasActionResultType(ActionResult.Type.END_OF_TURN);
        }

}
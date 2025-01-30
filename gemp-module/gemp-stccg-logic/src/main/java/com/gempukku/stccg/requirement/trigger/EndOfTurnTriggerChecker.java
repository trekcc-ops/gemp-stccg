package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class EndOfTurnTriggerChecker implements TriggerChecker {

        @Override
        public boolean accepts(ActionContext actionContext) {
            return actionContext.hasActionResultType(ActionResult.Type.END_OF_TURN);
        }

}
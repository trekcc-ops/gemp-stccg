package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class ConditionTrigger implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "requires");

        final Requirement[] requirements = environment.getRequirementsFromJSON(value);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                return RequirementUtils.acceptsAllRequirements(requirements, actionContext);
            }
        };
    }
}

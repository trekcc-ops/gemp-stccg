package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public class PerTurnLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "limit");

        final int limit = environment.getInteger(object.get("limit"), "limit", 1);

        return (actionContext) -> actionContext.getSource().checkTurnLimit(limit);
    }
}
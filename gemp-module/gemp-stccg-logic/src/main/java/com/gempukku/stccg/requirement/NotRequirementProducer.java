package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public class NotRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires");

        final Requirement condition = environment.getRequirement((JSONObject) object.get("requires"));

        return (actionContext) -> !condition.accepts(actionContext);
    }
}

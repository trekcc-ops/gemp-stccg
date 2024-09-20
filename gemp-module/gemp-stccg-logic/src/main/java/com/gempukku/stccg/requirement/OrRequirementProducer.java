package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public class OrRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires");

        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return (actionContext) -> actionContext.acceptsAnyRequirements(requirements);
    }
}

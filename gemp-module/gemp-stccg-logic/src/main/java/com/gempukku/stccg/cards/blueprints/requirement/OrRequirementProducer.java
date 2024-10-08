package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class OrRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "requires");

        final Requirement[] requirements = new RequirementFactory().getRequirements(node);

        return (actionContext) -> actionContext.acceptsAnyRequirements(requirements);
    }
}
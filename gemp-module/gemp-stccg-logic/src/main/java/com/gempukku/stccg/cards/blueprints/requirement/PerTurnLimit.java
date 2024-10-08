package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class PerTurnLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "limit");

        final int limit = BlueprintUtils.getInteger(node, "limit", 1);

        return (actionContext) -> actionContext.getSource().checkTurnLimit(limit);
    }
}
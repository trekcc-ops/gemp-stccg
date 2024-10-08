package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class IsOwnerRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node);

        return actionContext -> actionContext.getPerformingPlayerId().equals(actionContext.getSource().getOwnerName());
    }
}
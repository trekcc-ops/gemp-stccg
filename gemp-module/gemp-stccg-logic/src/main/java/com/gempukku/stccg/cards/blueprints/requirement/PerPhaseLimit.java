package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

public class PerPhaseLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "limit", "perPlayer");

        final int limit = BlueprintUtils.getInteger(node, "limit", 1);
        final boolean perPlayer = BlueprintUtils.getBoolean(node, "perPlayer", false);

        return (actionContext) -> {
            if (perPlayer)
                return actionContext.getSource().checkPhaseLimit(actionContext.getPerformingPlayerId() + "_", limit);
            else
                return actionContext.getSource().checkPhaseLimit(limit);
        };
    }
}
package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class PerPhaseLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "limit", "perPlayer");

        final int limit = environment.getInteger(node, "limit", 1);
        final boolean perPlayer = environment.getBoolean(node, "perPlayer", false);

        return (actionContext) -> {
            if (perPlayer)
                return actionContext.getSource().checkPhaseLimit(actionContext.getPerformingPlayerId() + "_", limit);
            else
                return actionContext.getSource().checkPhaseLimit(limit);
        };
    }
}

package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.common.filterable.Phase;

public class PhaseRequirement extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "phase");

        final Phase phase = BlueprintUtils.getEnum(Phase.class, node.get("phase").textValue(), "phase");
        return (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase;
    }
}
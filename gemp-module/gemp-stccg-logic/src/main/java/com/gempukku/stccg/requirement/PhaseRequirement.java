package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;

public class PhaseRequirement extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "phase");

        final Phase phase = environment.getEnum(Phase.class, node.get("phase").textValue(), "phase");
        return (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase;
    }
}

package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;

public class NotRequirementProducer extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "requires");

        final Requirement condition = new CardBlueprintFactory().getRequirement(node.get("requires"));

        return (actionContext) -> !condition.accepts(actionContext);
    }
}
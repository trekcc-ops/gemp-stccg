package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class NextTribbleInSequenceRequirement extends RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "value");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("value"));

        return actionContext -> {
            if (actionContext instanceof TribblesActionContext context)
                return context.getGameState().getNextTribbleInSequence() ==
                        valueSource.evaluateExpression(context, null);
            else return false;
        };
    }
}
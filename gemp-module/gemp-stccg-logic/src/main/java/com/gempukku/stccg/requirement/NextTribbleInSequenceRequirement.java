package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

public class NextTribbleInSequenceRequirement extends RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "value");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("value"), environment);

        return actionContext -> {
            if (actionContext instanceof TribblesActionContext context)
                return context.getGameState().getNextTribbleInSequence() ==
                        valueSource.evaluateExpression(context, null);
            else return false;
        };
    }
}

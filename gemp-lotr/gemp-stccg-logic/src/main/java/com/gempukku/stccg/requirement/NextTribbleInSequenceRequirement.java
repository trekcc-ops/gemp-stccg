package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class NextTribbleInSequenceRequirement extends RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "value");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("value"), environment);

        return actionContext -> {
            if (actionContext instanceof TribblesActionContext context)
                return context.getGameState().getNextTribbleInSequence() ==
                        valueSource.evaluateExpression(context, null);
            else return false;
        };
    }
}

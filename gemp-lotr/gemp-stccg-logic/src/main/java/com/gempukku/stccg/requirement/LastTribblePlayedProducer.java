package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class LastTribblePlayedProducer extends TribblesRequirementProducer{
    @Override
    public TribblesRequirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "value");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("value"), environment);

        return actionContext -> actionContext.getGameState().getLastTribblePlayed() ==
                valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }
}

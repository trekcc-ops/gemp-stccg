package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.TribblesGame;
import org.json.simple.JSONObject;

public class NextTribbleInSequenceRequirement implements RequirementProducer{
    @Override
    public Requirement<TribblesGame> getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "value");

        final ValueSource<TribblesGame> valueSource = ValueResolver.resolveEvaluator(object.get("value"), environment);

        return actionContext -> actionContext.getGame().getGameState().getNextTribbleInSequence() ==
                valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }
}

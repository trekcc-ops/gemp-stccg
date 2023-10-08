package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.TribblesGame;
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

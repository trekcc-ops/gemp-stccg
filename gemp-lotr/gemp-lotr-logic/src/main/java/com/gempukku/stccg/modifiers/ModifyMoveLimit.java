package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class ModifyMoveLimit implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return (actionContext) -> {
            final Evaluator evaluator = valueSource.getEvaluator(actionContext);
            return new MoveLimitModifier(actionContext.getSource(), evaluator.evaluateExpression(actionContext.getGame(), null));
        };
    }
}

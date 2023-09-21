package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.modifiers.lotronly.MoveLimitModifier;
import com.gempukku.lotro.evaluator.Evaluator;
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

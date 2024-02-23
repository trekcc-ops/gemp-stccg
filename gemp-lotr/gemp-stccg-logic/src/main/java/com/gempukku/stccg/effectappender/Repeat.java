package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class Repeat implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "effect");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final JSONObject effect = (JSONObject) effectObject.get("effect");

        final EffectAppender effectAppender = environment.getEffectAppenderFactory().getEffectAppender(effect, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final int count = amountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (count > 0) {
                    SubAction subAction = action.createSubAction();
                    for (int i = 0; i < count; i++)
                        effectAppender.appendEffect(cost, subAction, actionContext);
                    return new StackActionEffect(actionContext.getGame(), subAction);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return effectAppender.isPlayableInFull(actionContext);
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return effectAppender.isPlayabilityCheckedForEffect();
            }
        };
    }
}

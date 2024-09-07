package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import org.json.simple.JSONObject;

public class Repeat implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "amount", "effect");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final EffectAppender effectAppender =
                environment.getEffectAppenderFactory().getEffectAppender((JSONObject) effectObject.get("effect"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final int count = amountSource.evaluateExpression(context, null);
                if (count > 0) {
                    SubAction subAction = action.createSubAction();
                    for (int i = 0; i < count; i++)
                        effectAppender.appendEffect(cost, subAction, context);
                    return new StackActionEffect(context.getGame(), subAction);
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

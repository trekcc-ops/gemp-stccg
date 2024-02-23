package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import org.json.simple.JSONObject;

public class SetNextTribble implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext actionContext) {
                if (actionContext instanceof TribblesActionContext) {
                    int value = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect() {
                            ((TribblesActionContext) actionContext).getGame().getGameState().setNextTribbleInSequence(value);
                        }
                    };
                } else {
                    return null;
                }
            }
        };
    }
}

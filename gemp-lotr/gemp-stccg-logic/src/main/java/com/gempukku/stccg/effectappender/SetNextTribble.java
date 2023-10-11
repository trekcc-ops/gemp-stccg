package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.ValueSource;
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
                                          TribblesActionContext actionContext) {
                int value = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        actionContext.getGame().getGameState().setNextTribbleInSequence(value);
                    }
                };
            }
        };
    }
}

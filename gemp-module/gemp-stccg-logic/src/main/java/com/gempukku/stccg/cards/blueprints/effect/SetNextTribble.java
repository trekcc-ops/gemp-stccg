package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class SetNextTribble implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext context) {
                if (context instanceof TribblesActionContext) {
                    int value = amount.evaluateExpression(context, null);

                    return new UnrespondableEffect(context) {
                        @Override
                        protected void doPlayEffect() {
                            ((TribblesActionContext) context).getGame().getGameState().setNextTribbleInSequence(value);
                        }
                    };
                } else {
                    return null;
                }
            }
        };
    }
}
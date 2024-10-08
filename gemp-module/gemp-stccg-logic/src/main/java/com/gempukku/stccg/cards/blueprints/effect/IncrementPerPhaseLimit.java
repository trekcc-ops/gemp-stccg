package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementPhaseLimitEffect;

public class IncrementPerPhaseLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "limit", "perPlayer");

        final ValueSource limitSource = ValueResolver.resolveEvaluator(effectObject.get("limit"), 1, environment);
        final boolean perPlayer = environment.getBoolean(effectObject, "perPlayer", false);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final int limit = limitSource.evaluateExpression(context, context.getSource());

                if (perPlayer)
                    return new IncrementPhaseLimitEffect(context, context.getPerformingPlayerId() + "_", limit);
                else
                    return new IncrementPhaseLimitEffect(context, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final int limit = limitSource.evaluateExpression(actionContext, actionContext.getSource());

                if (perPlayer)
                    return actionContext.getSource().checkPhaseLimit(actionContext.getPerformingPlayerId() + "_",limit);
                else
                    return actionContext.getSource().checkPhaseLimit(limit);
            }
        };
    }

}
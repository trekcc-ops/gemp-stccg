package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementPhaseLimitEffect;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerPhaseLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "limit", "perPlayer");

        final ValueSource limitSource = ValueResolver.resolveEvaluator(effectObject.get("limit"), 1, environment);
        final boolean perPlayer = environment.getBoolean(effectObject.get("perPlayer"), "perPlayer", false);

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
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(),
                            actionContext.getPerformingPlayerId() + "_", limit);
                else
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), limit);
            }
        };
    }

}

package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.IncrementPhaseLimitEffect;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerPhaseLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "limit", "perPlayer");

        final ValueSource limitSource = ValueResolver.resolveEvaluator(effectObject.get("limit"), 1, environment);
        final boolean perPlayer = FieldUtils.getBoolean(effectObject.get("perPlayer"), "perPlayer", false);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Evaluator evaluator = limitSource.getEvaluator(actionContext);
                final int limit = evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());

                if (perPlayer)
                    return new IncrementPhaseLimitEffect(actionContext, actionContext.getPerformingPlayer() + "_", limit);
                else
                    return new IncrementPhaseLimitEffect(actionContext, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Evaluator evaluator = limitSource.getEvaluator(actionContext);
                final int limit = evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());

                if (perPlayer)
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), actionContext.getPerformingPlayer() + "_", limit);
                else
                    return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), limit);
            }
        };
    }

}

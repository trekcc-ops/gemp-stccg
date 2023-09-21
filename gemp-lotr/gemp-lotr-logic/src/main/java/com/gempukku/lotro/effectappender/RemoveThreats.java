package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.RemoveThreatsEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class RemoveThreats implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final Evaluator evaluator = valueSource.getEvaluator(null);
                return new RemoveThreatsEffect(actionContext.getSource(), evaluator.evaluateExpression(actionContext.getGame(), null));
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                final DefaultGame game = actionContext.getGame();
                final int threats = evaluator.evaluateExpression(game, null);
                return game.getModifiersQuerying().canRemoveThreat(game, actionContext.getSource())
                        && game.getGameState().getThreats() >= threats;
            }
        };
    }

}

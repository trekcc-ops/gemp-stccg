package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.RemoveTwilightEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class RemoveTwilight implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount");
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                int value = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return new RemoveTwilightEffect(value);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                int value = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGame().getGameState().getTwilightPool() >= value;
            }
        };
    }
}

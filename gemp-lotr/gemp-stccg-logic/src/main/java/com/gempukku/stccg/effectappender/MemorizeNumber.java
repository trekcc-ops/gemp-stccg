package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class MemorizeNumber implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "memory");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        DefaultGame game = actionContext.getGame();
                        final int value = amountSource.getEvaluator(actionContext).evaluateExpression(game, null);
                        actionContext.setValueToMemory(memory, String.valueOf(value));
                    }
                };
            }
        };
    }

}

package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.rules.GameUtils;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class ForEachPlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "effect");

        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                SubAction subAction = action.createSubAction();
                for (String playerId : actionContext.getGame().getAllPlayers()) {
                    Arrays.stream(effectAppenders).forEach(effectAppender ->
                            effectAppender.appendEffect(cost, action, actionContext.createDelegateContext(playerId)));
                }
                return new StackActionEffect(actionContext.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                for (String playerId : actionContext.getGame().getAllPlayers()) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        if (!effectAppender.isPlayableInFull(actionContext.createDelegateContext(playerId)))
                            return false;
                    }
                }
                return true;
            }
        };
    }
}

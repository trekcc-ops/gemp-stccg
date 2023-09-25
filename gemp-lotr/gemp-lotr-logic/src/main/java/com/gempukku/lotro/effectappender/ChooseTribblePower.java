package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.choose.ChooseTribblePowerEffect;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ChooseTribblePower implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new ChooseTribblePowerEffect(actionContext.getPerformingPlayer()) {
                    @Override
                    protected void powerChosen(String playerId) {
                        actionContext.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}

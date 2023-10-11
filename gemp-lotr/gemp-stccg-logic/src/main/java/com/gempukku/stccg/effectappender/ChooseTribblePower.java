package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseTribblePowerEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ChooseTribblePower implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, TribblesActionContext actionContext) {
                return new ChooseTribblePowerEffect(actionContext) {
                    @Override
                    protected void powerChosen(String playerId) {
                        actionContext.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}

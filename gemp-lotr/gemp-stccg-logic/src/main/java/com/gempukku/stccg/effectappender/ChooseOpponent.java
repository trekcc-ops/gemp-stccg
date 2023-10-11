package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseOpponentEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ChooseOpponent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new ChooseOpponentEffect(actionContext) {
                    @Override
                    protected void playerChosen(String opponentId) {
                        actionContext.setValueToMemory(memorize, opponentId);
                    }
                };
            }
        };
    }

}

package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.CancelEventEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.results.PlayEventResult;
import org.json.simple.JSONObject;

public class CancelEvent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final PlayEventResult playEventResult = (PlayEventResult) actionContext.getEffectResult();
                return new CancelEventEffect(actionContext, playEventResult);
            }
        };
    }
}

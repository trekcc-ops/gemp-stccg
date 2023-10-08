package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effects.CancelEventEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.PlayEventResult;
import org.json.simple.JSONObject;

public class CancelEvent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final PlayEventResult playEventResult = (PlayEventResult) actionContext.getEffectResult();
                return new CancelEventEffect(actionContext.getSource(), playEventResult);
            }
        };
    }
}

package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.effects.ActivateDiscardTribblePowerEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ActivateTribblePowerAppender implements EffectAppenderProducer {

    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
//                final Map<TribblePower, ActivateTribblePowerEffect> activateEffects = new HashMap<>();

                if (actionContext.getSource().getBlueprint().getTribblePower() == TribblePower.DISCARD)
                    return new ActivateDiscardTribblePowerEffect(action, actionContext.getSource());
                else
                    return new ActivateDiscardTribblePowerEffect(action, actionContext.getSource());
            }
        };
    }

}

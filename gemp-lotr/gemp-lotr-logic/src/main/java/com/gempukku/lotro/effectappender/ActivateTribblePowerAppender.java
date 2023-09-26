package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.effects.*;
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

                TribblePower tribblePower = actionContext.getSource().getBlueprint().getTribblePower();
                LotroPhysicalCard actionSource = actionContext.getSource();

                if (tribblePower == TribblePower.DISCARD)
                    return new ActivateDiscardTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.DRAW)
                    return new ActivateDrawTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.CYCLE)
                    return new ActivateCycleTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.EXCHANGE)
                    return new ActivateExchangeTribblePowerEffect(action, actionSource);
                else
                    return new ActivateCycleTribblePowerEffect(action, actionSource);
            }
        };
    }

}
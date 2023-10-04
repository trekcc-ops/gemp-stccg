package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PhysicalCard;
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
                PhysicalCard actionSource = actionContext.getSource();

                if (tribblePower == TribblePower.AVALANCHE)
                    return new ActivateAvalancheTribblePowerEffect(action, actionSource);
                if (tribblePower == TribblePower.CONVERT)
                    return new ActivateConvertTribblePowerEffect(action, actionSource);
                if (tribblePower == TribblePower.CYCLE)
                    return new ActivateCycleTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.DISCARD)
                    return new ActivateDiscardTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.DRAW)
                    return new ActivateDrawTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.EVOLVE)
                    return new ActivateEvolveTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.EXCHANGE)
                    return new ActivateExchangeTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.FAMINE)
                    return new ActivateFamineTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.FREEZE) // TODO- Freeze not yet implemented
                    return new ActivateCycleTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.GENEROSITY)
                    return new ActivateGenerosityTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.KILL)
                    return new ActivateKillTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.KINDNESS)
                    return new ActivateKindnessTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.LAUGHTER)
                    return new ActivateLaughterTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.MASAKA)
                    return new ActivateMasakaTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.MUTATE)
                    return new ActivateMutateTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.POISON)
                    return new ActivatePoisonTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.PROCESS)
                    return new ActivateProcessTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.RECYCLE)
                    return new ActivateRecycleTribblePowerEffect(action, actionSource);
                else if (tribblePower == TribblePower.REVERSE)
                    return new ActivateReverseTribblePowerEffect(action, actionSource);
                else
                    throw new RuntimeException(
                            "Code not yet implemented for Tribble power " + tribblePower.getHumanReadable());
            }
        };
    }

}
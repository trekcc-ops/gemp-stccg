package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.tribblepower.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.common.filterable.TribblePower;

public class ActivateTribblePowerEffectBlueprint extends DelayedEffectBlueprint {

    public ActivateTribblePowerEffectBlueprint(JsonNode effectObject) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(effectObject);
    }

    @Override
    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {

        TribblePower tribblePower = context.getSource().getBlueprint().getTribblePower();
        if (context instanceof TribblesActionContext)
            return createTribblePowerEffect(tribblePower, (TribblesActionContext) context, action);
        else return null;
    }

    private Effect createTribblePowerEffect(TribblePower tribblePower, TribblesActionContext actionContext,
                                            CostToEffectAction action) {

        if (tribblePower == TribblePower.ACQUIRE)
            return new ActivateAcquireTribblePowerEffect(action, actionContext);
        if (tribblePower == TribblePower.AVALANCHE)
            return new ActivateAvalancheTribblePowerEffect(action, actionContext);
        if (tribblePower == TribblePower.CONVERT)
            return new ActivateConvertTribblePowerEffect(action, actionContext);
        if (tribblePower == TribblePower.CYCLE)
            return new ActivateCycleTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.DISCARD)
            return new ActivateDiscardTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.DRAW)
            return new ActivateDrawTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.EVOLVE)
            return new ActivateEvolveTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.EXCHANGE)
            return new ActivateExchangeTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.FAMINE)
            return new ActivateFamineTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.FREEZE) // TODO- Freeze not yet implemented
            return new ActivateCycleTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.GENEROSITY)
            return new ActivateGenerosityTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.KILL)
            return new ActivateKillTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.KINDNESS)
            return new ActivateKindnessTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.LAUGHTER)
            return new ActivateLaughterTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.MASAKA)
            return new ActivateMasakaTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.MUTATE)
            return new ActivateMutateTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.POISON)
            return new ActivatePoisonTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.PROCESS)
            return new ActivateProcessTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.RECYCLE)
            return new ActivateRecycleTribblePowerEffect(action, actionContext);
        else if (tribblePower == TribblePower.REVERSE)
            return new ActivateReverseTribblePowerEffect(action, actionContext);
        else
            throw new RuntimeException(
                    "Code not yet implemented for Tribble power " + tribblePower.getHumanReadable());
    }
}
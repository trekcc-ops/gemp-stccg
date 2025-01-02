package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.tribblepower.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.LinkedList;
import java.util.List;

public class ActivateTribblePowerEffectBlueprint extends DelayedEffectBlueprint {

    public ActivateTribblePowerEffectBlueprint(JsonNode effectObject) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(effectObject);
    }

    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidCardDefinitionException, InvalidGameLogicException {

        List<Action> result = new LinkedList<>();
        TribblePower tribblePower = context.getSource().getBlueprint().getTribblePower();
        if (context instanceof TribblesActionContext tribblesContext) {

            Action activateAction = switch (tribblePower) {
                case AVALANCHE -> new ActivateAvalancheTribblePowerAction(tribblesContext, tribblePower);
                case CONVERT -> new ActivateConvertTribblePowerAction(tribblesContext, tribblePower);
                case CYCLE -> new ActivateCycleTribblePowerAction(tribblesContext, tribblePower);
                case DISCARD -> new ActivateDiscardTribblePowerAction(tribblesContext, tribblePower);
                case DRAW -> new ActivateDrawTribblePowerAction(tribblesContext, tribblePower);
                case EVOLVE -> new ActivateEvolveTribblePowerAction(tribblesContext, tribblePower);
                case FAMINE -> new ActivateFamineTribblePowerAction(tribblesContext, tribblePower);
                case GENEROSITY -> new ActivateGenerosityTribblePowerAction(tribblesContext, tribblePower);
                case KILL -> new ActivateKillTribblePowerAction(tribblesContext, tribblePower);
                case KINDNESS -> new ActivateKindnessTribblePowerAction(tribblesContext, tribblePower);
                case LAUGHTER -> new ActivateLaughterTribblePowerAction(tribblesContext, tribblePower);
                case MASAKA -> new ActivateMasakaTribblePowerAction(tribblesContext, tribblePower);
                case MUTATE -> new ActivateMutateTribblePowerAction(tribblesContext, tribblePower);
                case POISON -> new ActivatePoisonTribblePowerAction(tribblesContext, tribblePower);
                case PROCESS -> new ActivateProcessTribblePowerAction(tribblesContext, tribblePower);
                case RECYCLE -> new ActivateRecycleTribblePowerAction(tribblesContext, tribblePower);
                case REVERSE -> new ActivateReverseTribblePowerAction(tribblesContext, tribblePower);
                default -> throw new InvalidCardDefinitionException(
                        "Code not yet implemented for Tribble power " + tribblePower.getHumanReadable());
            };
            result.add(activateAction);
            return result;
        } else throw new InvalidCardDefinitionException(
                "Could not create Tribbles power effect for a non-Tribbles context");
    }
}
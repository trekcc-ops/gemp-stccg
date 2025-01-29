package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class EffectWithCostBlueprint extends DelayedEffectBlueprint {
    private final List<EffectBlueprint> _effectBlueprints;
    private final List<EffectBlueprint> _costAppenders;
    private final Requirement[] _requirements;

    public EffectWithCostBlueprint(@JsonProperty("requires")
                                   JsonNode requirementNode,
                                   @JsonProperty("cost")
                                   List<EffectBlueprint> costs,
                                   @JsonProperty("effect")
                                   List<EffectBlueprint> effects) throws InvalidCardDefinitionException {
        _requirements = RequirementFactory.getRequirements(requirementNode);
        _costAppenders = (costs == null) ? new LinkedList<>() : costs;
        _effectBlueprints = (effects == null) ? new LinkedList<>() : effects;
    }
    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context) throws PlayerNotFoundException {

        List<Action> result = new LinkedList<>();
        if(requirementsNotMet(context)) {
            SubAction subAction = new SubAction(action, context, _costAppenders, _effectBlueprints);
            result.add(subAction);
        }
        return result;
    }


    private boolean requirementsNotMet(ActionContext actionContext) {
        return (!actionContext.acceptsAllRequirements(List.of(_requirements)));
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {

        if(requirementsNotMet(actionContext))
            return false;

        for (EffectBlueprint costAppender : _costAppenders) {
            if (!costAppender.isPlayableInFull(actionContext))
                return false;
        }

        for (EffectBlueprint effectBlueprint : _effectBlueprints) {
            if (effectBlueprint.isPlayabilityCheckedForEffect()
                    && !effectBlueprint.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }
}
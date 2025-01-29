package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class SubActionWithCostBlueprint extends DelayedEffectBlueprint {
    private final List<SubActionBlueprint> _subActionBlueprints;
    private final List<SubActionBlueprint> _costAppenders;
    private final Requirement[] _requirements;

    public SubActionWithCostBlueprint(@JsonProperty("requires")
                                   JsonNode requirementNode,
                                      @JsonProperty("cost")
                                   List<SubActionBlueprint> costs,
                                      @JsonProperty("effect")
                                   List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        _requirements = RequirementFactory.getRequirements(requirementNode);
        _costAppenders = (costs == null) ? new LinkedList<>() : costs;
        _subActionBlueprints = (effects == null) ? new LinkedList<>() : effects;
    }
    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context) throws PlayerNotFoundException {

        List<Action> result = new LinkedList<>();
        if(requirementsNotMet(context)) {
            SubAction subAction = new SubAction(action, context, _costAppenders, _subActionBlueprints);
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

        for (SubActionBlueprint costAppender : _costAppenders) {
            if (!costAppender.isPlayableInFull(actionContext))
                return false;
        }

        for (SubActionBlueprint subActionBlueprint : _subActionBlueprints) {
            if (subActionBlueprint.isPlayabilityCheckedForEffect()
                    && !subActionBlueprint.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }
/*
    List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException;
  */
}
package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SubActionWithCostBlueprint extends DelayedEffectBlueprint {
    private final List<SubActionBlueprint> _subActionBlueprints;
    private final List<SubActionBlueprint> _costAppenders;
    private final List<Requirement> _requirements;

    public SubActionWithCostBlueprint(@JsonProperty("requires")
                                   List<Requirement> requirements,
                                      @JsonProperty("cost")
                                   List<SubActionBlueprint> costs,
                                      @JsonProperty("effect")
                                   List<SubActionBlueprint> effects) {
        _requirements = Objects.requireNonNullElse(requirements, new LinkedList<>());
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
        return (!actionContext.acceptsAllRequirements(_requirements));
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
}
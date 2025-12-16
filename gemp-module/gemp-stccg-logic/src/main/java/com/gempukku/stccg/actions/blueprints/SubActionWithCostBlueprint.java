package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SubActionWithCostBlueprint implements SubActionBlueprint {
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
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context) {

        List<Action> result = new LinkedList<>();
        if(requirementsNotMet(cardGame, context)) {
            SubAction subAction = new SubAction(cardGame, action, context, _costAppenders, _subActionBlueprints);
            result.add(subAction);
        }
        return result;
    }


    private boolean requirementsNotMet(DefaultGame cardGame, ActionContext actionContext) {
        return (!actionContext.acceptsAllRequirements(cardGame, _requirements));
    }


    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {

        if(requirementsNotMet(cardGame, actionContext))
            return false;

        for (SubActionBlueprint costAppender : _costAppenders) {
            if (!costAppender.isPlayableInFull(cardGame, actionContext))
                return false;
        }

        for (SubActionBlueprint subActionBlueprint : _subActionBlueprints) {
            if (subActionBlueprint.isPlayabilityCheckedForEffect()
                    && !subActionBlueprint.isPlayableInFull(cardGame, actionContext))
                return false;
        }

        return true;
    }
}
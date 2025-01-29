package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.effect.DelayedEffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.SubActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class DefaultActionBlueprint implements ActionBlueprint {
    private final List<Requirement> requirements = new LinkedList<>();

    protected final List<SubActionBlueprint> costs = new LinkedList<>();
    protected final List<SubActionBlueprint> effects = new LinkedList<>();

    protected String _text;

    public DefaultActionBlueprint(String text, int limitPerTurn, Phase phase) {
            if (text != null)
                setText(text);
            if (limitPerTurn > 0)
                setTurnLimit(limitPerTurn);
            if (phase != null)
                addRequirement(
                        (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
    }

    public void setText(String text) {
        this._text = text;
    }

    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void addCost(SubActionBlueprint subActionBlueprint) {
        costs.add(subActionBlueprint);
    }

    public void addEffect(SubActionBlueprint subActionBlueprint) {
        effects.add(subActionBlueprint);
    }

    @Override
    public boolean isValid(ActionContext actionContext) {
        return actionContext.acceptsAllRequirements(requirements);
    }

    @Override
    public void appendActionToContext(TopLevelSelectableAction action, ActionContext actionContext) {
        if (_text != null)
            action.setText(actionContext.substituteText(_text));

        costs.forEach(cost -> cost.addEffectToAction(true, action, actionContext));

        effects.forEach(actionEffect -> actionEffect.addEffectToAction(false, action, actionContext));
    }

    public void processRequirementsCostsAndEffects(List<Requirement> requirements, List<SubActionBlueprint> costs,
                                                   List<SubActionBlueprint> effects)
            throws InvalidCardDefinitionException {

        if ((costs == null || costs.isEmpty()) && (effects == null || effects.isEmpty()))
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        if (requirements != null && !requirements.isEmpty()) {
            for (Requirement requirement : requirements) {
                addRequirement(requirement);
            }
        }

        if (costs != null && !costs.isEmpty()) {
            for (SubActionBlueprint costBlueprint : costs) {
                addRequirement(costBlueprint::isPlayableInFull);
                addCost(costBlueprint);
            }
        }

        if (effects != null && !effects.isEmpty()) {
            for (SubActionBlueprint blueprint : effects) {
                if (blueprint.isPlayabilityCheckedForEffect())
                    addRequirement(blueprint::isPlayableInFull);
                addEffect(blueprint);
            }
        }
    }






    protected abstract TopLevelSelectableAction createActionAndAppendToContext(PhysicalCard card, ActionContext context);

    public void setTurnLimit(int limitPerTurn) {
        addRequirement((actionContext) ->
                actionContext.getSource().checkTurnLimit(actionContext.getGame(), limitPerTurn));
        addCost(
            new DelayedEffectBlueprint() {
                @Override
                protected List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
                        throws PlayerNotFoundException {
                    Action usageLimitAction = new UseOncePerTurnAction(
                            action, action.getPerformingCard(), actionContext.getPerformingPlayer());
                    return Collections.singletonList(usageLimitAction);
                }
            });
    }

    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, null));
    }

    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, actionResult));
    }


    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, String playerId, ActionResult actionResult) {
        return createActionAndAppendToContext(card, new DefaultActionContext(playerId, card, actionResult));
    }
}
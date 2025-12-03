package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.CostCanBePaidRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.TurnLimitRequirement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class DefaultActionBlueprint implements ActionBlueprint {
    protected final List<Requirement> _requirements = new LinkedList<>();

    protected final List<SubActionBlueprint> costs = new LinkedList<>();
    protected final List<SubActionBlueprint> effects = new LinkedList<>();

    public DefaultActionBlueprint(int limitPerTurn) {
        if (limitPerTurn > 0)
            setTurnLimit(limitPerTurn);
    }
    public DefaultActionBlueprint(int limitPerTurn, List<SubActionBlueprint> costs,
                                  List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        this(limitPerTurn);

        if ((costs == null || costs.isEmpty()) && (effects == null || effects.isEmpty()))
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        if (costs != null && !costs.isEmpty()) {
            for (SubActionBlueprint costBlueprint : costs) {
                addRequirement(new CostCanBePaidRequirement(costBlueprint));
                addCost(costBlueprint);
            }
        }

        if (effects != null && !effects.isEmpty()) {
            for (SubActionBlueprint blueprint : effects) {
                if (blueprint.isPlayabilityCheckedForEffect())
                    addRequirement(new CostCanBePaidRequirement(blueprint));
                addEffect(blueprint);
            }
        }
    }

    public void addRequirement(Requirement requirement) {
        this._requirements.add(requirement);
    }

    public void addCost(SubActionBlueprint subActionBlueprint) {
        costs.add(subActionBlueprint);
    }

    public void addEffect(SubActionBlueprint subActionBlueprint) {
        effects.add(subActionBlueprint);
    }

    @Override
    public boolean isValid(DefaultGame cardGame, ActionContext actionContext) {
        return actionContext.acceptsAllRequirements(cardGame, _requirements);
    }

    @Override
    public void appendActionToContext(DefaultGame cardGame, TopLevelSelectableAction action,
                                      ActionContext actionContext) {
        costs.forEach(cost -> cost.addEffectToAction(cardGame, true, action, actionContext));
        effects.forEach(actionEffect -> actionEffect.addEffectToAction(cardGame, false, action, actionContext));
    }

    public abstract TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                          PhysicalCard thisCard);

    public void setTurnLimit(int limitPerTurn) {
        addRequirement(new TurnLimitRequirement(limitPerTurn));
        addCost(
                (cardGame, action, actionContext) -> {
                    Action usageLimitAction = new UseOncePerTurnAction(cardGame,
                            action, action.getPerformingCard(), actionContext.getPerformingPlayerId());
                    return Collections.singletonList(usageLimitAction);
                });
    }


}
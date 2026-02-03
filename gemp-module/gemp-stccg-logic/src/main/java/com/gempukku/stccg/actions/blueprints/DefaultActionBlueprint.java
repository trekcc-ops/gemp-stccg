package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.CostCanBePaidRequirement;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;

public abstract class DefaultActionBlueprint implements ActionBlueprint {
    protected final List<Requirement> _requirements = new LinkedList<>();
    protected final List<SubActionBlueprint> costs = new LinkedList<>();
    protected final List<SubActionBlueprint> _effects = new LinkedList<>();
    private final PlayerSource _performingPlayer;
    private int _blueprintId;

    protected DefaultActionBlueprint(int limitPerTurn, PlayerSource performingPlayer) {
        if (limitPerTurn > 0) {
            costs.add(new UsePerTurnLimitActionBlueprint(this, limitPerTurn));
        }
        _performingPlayer = performingPlayer;
    }

    protected DefaultActionBlueprint(int limitPerTurn, List<SubActionBlueprint> costs,
                                  List<SubActionBlueprint> effects,
                                  PlayerSource playerSource) throws InvalidCardDefinitionException {
        this(limitPerTurn, playerSource);

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
        _effects.add(subActionBlueprint);
    }

    @Override
    public boolean isValid(DefaultGame cardGame, ActionContext actionContext) {
        boolean isValidPlayer =
                _performingPlayer.isPlayer(actionContext.getPerformingPlayerId(), cardGame, actionContext);
        if (!isValidPlayer) {
            return false;
        } else {
            return actionContext.acceptsAllRequirements(cardGame, _requirements);
        }
    }

    @Override
    public void appendActionToContext(DefaultGame cardGame, ActionWithSubActions action,
                                      ActionContext actionContext) {
        costs.forEach(cost -> cost.addEffectToAction(cardGame, true, action, actionContext));
        _effects.forEach(actionEffect -> actionEffect.addEffectToAction(cardGame, false, action, actionContext));
    }

    public abstract TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                          PhysicalCard thisCard);


}
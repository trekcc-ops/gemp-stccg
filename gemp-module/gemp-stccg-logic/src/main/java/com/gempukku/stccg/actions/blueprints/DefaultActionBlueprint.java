package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.CostCanBePaidRequirement;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class DefaultActionBlueprint implements ActionBlueprint {
    protected final List<Requirement> _requirements = new LinkedList<>();
    protected final List<SubActionBlueprint> costs = new LinkedList<>();
    protected final List<SubActionBlueprint> _effects = new LinkedList<>();
    private final PlayerSource _performingPlayer;
    private int _blueprintId;

    protected DefaultActionBlueprint(int limitPerTurn, PlayerSource performingPlayer) {
        if (limitPerTurn > 0)
            setTurnLimit(limitPerTurn);
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
        String performingPlayerName = _performingPlayer.getPlayerId(cardGame, actionContext);
        if (!performingPlayerName.equals(actionContext.getPerformingPlayerId())) {
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

    public void setTurnLimit(int limitPerTurn) {
        ActionBlueprint thisBlueprint = this;
        addCost(
                new SubActionBlueprint() {
                    @Override
                    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext actionContext) throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
                        Action usageLimitAction = new UseOncePerTurnAction(cardGame,
                                actionContext.getPerformingCard(cardGame), thisBlueprint, actionContext.getPerformingPlayerId());
                        return Collections.singletonList(usageLimitAction);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }

                    @Override
                    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {
                        try {
                            GameState gameState = cardGame.getGameState();
                            PhysicalCard thisCard = actionContext.getPerformingCard(cardGame);
                            LimitCounter counter = gameState.getUntilEndOfTurnLimitCounter(thisCard, thisBlueprint);
                            return counter.getUsedLimit() < limitPerTurn;
                        } catch(InvalidGameLogicException exp) {
                            cardGame.sendErrorMessage(exp);
                            return false;
                        }
                    }
                });
    }


}
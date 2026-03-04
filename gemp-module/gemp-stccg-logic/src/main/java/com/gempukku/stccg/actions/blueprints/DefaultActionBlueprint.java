package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.GameTextContext;
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

    protected DefaultActionBlueprint(PlayerSource performingPlayer) {
        _performingPlayer = performingPlayer;
    }

    protected DefaultActionBlueprint(List<SubActionBlueprint> costs,
                                     List<SubActionBlueprint> effects,
                                     PlayerSource playerSource) {
        this(playerSource);

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
                _effects.add(blueprint);
            }
        }
    }

    public void addRequirement(Requirement requirement) {
        _requirements.add(requirement);
    }

    public void addCost(SubActionBlueprint subActionBlueprint) {
        costs.add(subActionBlueprint);
    }

    @Override
    public boolean isValid(DefaultGame cardGame, GameTextContext context) {
        boolean isValidPlayer = _performingPlayer.isPlayer(context.yourName(), cardGame, context);
        if (!isValidPlayer) {
            return false;
        } else {
            return context.acceptsAllRequirements(cardGame, _requirements);
        }
    }

    protected boolean isActionForPlayer(String requestingPlayerName, DefaultGame cardGame, GameTextContext context) {
        return _performingPlayer.isPlayer(requestingPlayerName, cardGame, context);
    }

    protected void appendSubActions(ActionWithSubActions action) {
        costs.forEach(action::appendCost);
        _effects.forEach(action::appendSubAction);
    }

}
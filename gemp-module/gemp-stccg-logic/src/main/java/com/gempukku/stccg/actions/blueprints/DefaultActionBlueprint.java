package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.CostCanBePaidRequirement;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.Collection;
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

    protected void appendSubActions(ActionWithSubActions action) {
        costs.forEach(action::appendCost);
        _effects.forEach(action::appendSubAction);
    }

    @Override
    public Collection<ActionBlueprint> getAllTheoreticalSubActions() {
        Collection<ActionBlueprint> result = new ArrayList<>();
        result.addAll(costs);
        result.addAll(_effects);
        return result;
    }

}
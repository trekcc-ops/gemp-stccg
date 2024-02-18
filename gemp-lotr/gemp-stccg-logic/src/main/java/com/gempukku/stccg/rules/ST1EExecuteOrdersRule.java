package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;

import java.util.LinkedList;
import java.util.List;

public class ST1EExecuteOrdersRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ST1EExecuteOrdersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        final Phase currentPhase = actionsEnvironment.getGame().getGameState().getCurrentPhase();
                        if (currentPhase == Phase.EXECUTE_ORDERS) {
                            List<Action> result = new LinkedList<>();
                            // Beam - facility/ship
                            // Walk - personnel/equipment
                            // Dock & undock - ship
                            // Fly a starship - ship
                            // Land & take off - ship
                            // Attempt a mission - Away Team/ship (select mission)
                            // Initiate battle - Away Team/ship (select mission)
                            // Cloak - ship
                            for (PhysicalCard card : Filters.filterActive(actionsEnvironment.getGame(), Filters.your(playerId))) {
                                final List<? extends Action> phaseActions = card.getPhaseActionsInPlay(actionsEnvironment.getGame().getGameState().getPlayer(playerId));
                                if (phaseActions != null)
                                    result.addAll(phaseActions);
                            }
                            return result;
                        }
                        return null;
                    }
                });
    }
}

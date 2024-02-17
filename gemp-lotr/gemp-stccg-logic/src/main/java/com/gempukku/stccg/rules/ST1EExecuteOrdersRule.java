package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

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
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        final Phase currentPhase = game.getGameState().getCurrentPhase();
                        if (currentPhase == Phase.EXECUTE_ORDERS) {
                            List<Action> result = new LinkedList<>();
                            // Beam
                                // TODO - Current code enters an infinite loop if there's a card with transporters but no valid targets to beam
                                // TODO - Need to check to see if code to beam people back from the planet works. Prior to edits the cardsToBeam part of the code was never called.
                            // Walk
                            // Dock & undock
                            // Fly a starship
                            // Land & take off
                            // Attempt a mission
                            // Initiate battle
                            // Cloak
                            for (PhysicalCard card : Filters.filterActive(game, Filters.your(playerId))) {
                                final List<? extends Action> phaseActions = card.getPhaseActionsInPlay(game.getGameState().getPlayer(playerId));
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

package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromHandRule {
    private final ActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public ActivatePhaseActionsFromHandRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                            List<? extends Action> list = activatableCard.getPhaseActionsFromZone(playerId, Zone.HAND);
                            if (list != null) {
                                for (Action action : list) {
                                    action.setVirtualCardAction(true);
                                    result.add(action);
                                }
                            }
                        }
                        return result;
                    }
                });
    }
}

package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.common.filterable.Side;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.rules.lotronly.LotroGameUtils;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromHandRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsFromHandRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        List<Action> result = new LinkedList<>();
                        final Side side = LotroGameUtils.getSide(game, playerId);
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getHand(playerId), game, side)) {
                            List<? extends Action> list = activatableCard.getBlueprint().getPhaseActionsInHand(playerId, game, activatableCard);
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

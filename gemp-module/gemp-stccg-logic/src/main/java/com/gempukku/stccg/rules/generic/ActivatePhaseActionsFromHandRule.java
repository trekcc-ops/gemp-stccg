package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromHandRule extends GenericRule {

    public ActivatePhaseActionsFromHandRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getPhaseActions(String playerId) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
            List<? extends Action> list = card.getPhaseActionsFromZone(playerId, Zone.HAND);
            if (list != null) {
                for (Action action : list) {
                    action.setVirtualCardAction(true);
                    result.add(action);
                }
            }
        }
        return result;
    }
}
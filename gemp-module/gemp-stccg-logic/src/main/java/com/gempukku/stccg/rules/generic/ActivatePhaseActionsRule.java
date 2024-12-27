package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsRule extends GenericRule {
    public ActivatePhaseActionsRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(String playerId) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            for (PhysicalCard activatableCard : Filters.filter(_game.getGameState().getAllCardsInPlay(), _game,
                    Filters.and(Filters.owner(playerId), Filters.active))) {
                if (!activatableCard.hasTextRemoved(_game)) {

                }
            }
        }
        return result;
    }
}
package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.modifiers.ChangeAffiliationAction;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;

public class ST1EChangeAffiliationRule extends ST1ERule {

    ST1EChangeAffiliationRule(ST1EGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        LinkedList<TopLevelSelectableAction> result = new LinkedList<>();
        if (player.getPlayerId().equals(_game.getCurrentPlayerId())) {
            for (PhysicalCard card : Filters.filterYourActive(_game, player)) {
                if (card instanceof AffiliatedCard affiliatedCard && affiliatedCard.getAffiliationOptions().size() > 1) {
                    ChangeAffiliationAction action = new ChangeAffiliationAction(_game, player, affiliatedCard);
                    if (action.canBeInitiated(_game))
                        result.add(action);
                }
            }
        }
        return result;
    }

}
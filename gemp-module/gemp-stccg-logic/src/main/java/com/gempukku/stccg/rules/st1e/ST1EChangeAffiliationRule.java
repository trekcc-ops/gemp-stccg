package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.modifiers.ChangeAffiliationAction;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public class ST1EChangeAffiliationRule extends ST1ERule {

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        LinkedList<TopLevelSelectableAction> result = new LinkedList<>();
        if (player.getPlayerId().equals(cardGame.getCurrentPlayerId())) {
            for (PhysicalCard card : Filters.filterYourCardsInPlay(cardGame, player)) {
                if (card instanceof AffiliatedCard affiliatedCard && affiliatedCard.getAffiliationOptions().size() > 1) {
                    ChangeAffiliationAction action = new ChangeAffiliationAction(cardGame, player, affiliatedCard);
                    if (action.canBeInitiated(cardGame))
                        result.add(action);
                }
            }
        }
        return result;
    }

}
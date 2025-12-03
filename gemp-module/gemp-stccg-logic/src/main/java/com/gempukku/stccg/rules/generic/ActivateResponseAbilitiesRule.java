package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule extends GenericRule {

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(cardGame, Filters.and(Filters.owner(playerId), Filters.inPlay))) {
            if (!card.hasTextRemoved(cardGame))
                result.addAll(card.getOptionalResponseWhileInPlayActions(cardGame, playerId, actionResult));
        }
        return result;
    }
}
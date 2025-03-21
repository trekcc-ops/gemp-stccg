package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule extends GenericRule {
    public ActivateResponseAbilitiesRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(String playerId, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game, Filters.and(Filters.owner(playerId), Filters.active))) {
            if (!card.hasTextRemoved(_game))
                result.addAll(card.getOptionalResponseWhileInPlayActions(actionResult));
        }
        return result;
    }
}
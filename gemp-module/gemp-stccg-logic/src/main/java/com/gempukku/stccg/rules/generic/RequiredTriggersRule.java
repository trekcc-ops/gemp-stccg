package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class RequiredTriggersRule extends GenericRule {
    public RequiredTriggersRule(DefaultGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(ActionResult actionResult) {
        List<Action> result = new LinkedList<>();
        for (PhysicalCard card : Filters.filterCardsInPlay(_game)) {
            if (!card.hasTextRemoved(_game)) {
                result.addAll(card.getRequiredResponseActions(actionResult));
            }
        }
        return result;
    }
}
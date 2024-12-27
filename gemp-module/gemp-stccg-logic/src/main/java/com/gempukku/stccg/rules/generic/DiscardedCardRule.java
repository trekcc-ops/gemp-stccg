package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class DiscardedCardRule extends GenericRule {
    public DiscardedCardRule(DefaultGame game) { super(game); }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(ActionResult actionResult) {
        List<Action> result = new ArrayList<>();
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
            DiscardCardFromPlayResult discardResult = (DiscardCardFromPlayResult) actionResult;
            Action trigger = discardResult.getDiscardedCard().getDiscardedFromPlayTriggerAction(RequiredType.REQUIRED);
            if (trigger != null) result.add(trigger);
        }
        return result;
    }
}
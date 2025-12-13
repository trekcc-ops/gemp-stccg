package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class DiscardedCardRule extends GenericRule {

    @Override
    public List<TopLevelSelectableAction> getRequiredAfterTriggers(DefaultGame cardGame, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (actionResult.hasType(ActionResult.Type.JUST_DISCARDED_FROM_PLAY)) {
            if (actionResult instanceof DiscardCardFromPlayResult discardResult) {
                PhysicalCard discardedCard = discardResult.getDiscardedCard();

                ActionBlueprint actionBlueprint =
                        discardedCard.getBlueprint().getDiscardedFromPlayTrigger(RequiredType.REQUIRED);
                TopLevelSelectableAction trigger = (actionBlueprint == null) ?
                        null : actionBlueprint.createAction(cardGame, discardedCard.getOwnerName(), discardedCard);
                if (trigger != null) result.add(trigger);
            }
        }
        return result;
    }
}
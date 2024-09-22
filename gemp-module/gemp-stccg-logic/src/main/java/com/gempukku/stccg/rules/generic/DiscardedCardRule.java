package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayResult;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;
import java.util.List;

public class DiscardedCardRule extends GenericRule {
    public DiscardedCardRule(DefaultGame game) { super(game); }

    @Override
    public List<? extends Action> getRequiredAfterTriggers(EffectResult effectResult) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
            Action trigger = discardedCard.getDiscardedFromPlayTriggerAction(RequiredType.REQUIRED);
            if (trigger != null)
                return Collections.singletonList(trigger);
        }
        return null;
    }
}
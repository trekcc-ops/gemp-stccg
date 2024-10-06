package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collection;

public interface PreventableCardEffect {
    Collection<PhysicalCard> getAffectedCardsMinusPrevented();
    void preventEffect(PhysicalCard affectedCard);

}

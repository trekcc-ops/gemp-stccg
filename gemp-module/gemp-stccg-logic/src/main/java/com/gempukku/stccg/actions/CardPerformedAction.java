package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface CardPerformedAction extends Action {

    PhysicalCard getPerformingCard();
}
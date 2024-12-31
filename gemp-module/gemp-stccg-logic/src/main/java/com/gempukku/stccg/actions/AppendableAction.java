package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface AppendableAction extends Action {

    PhysicalCard getPerformingCard();
}
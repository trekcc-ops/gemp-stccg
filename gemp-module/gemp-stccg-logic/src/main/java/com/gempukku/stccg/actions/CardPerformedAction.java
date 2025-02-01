package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface CardPerformedAction extends Action {

    @JsonIdentityReference(alwaysAsId=true)
    PhysicalCard getPerformingCard();
}
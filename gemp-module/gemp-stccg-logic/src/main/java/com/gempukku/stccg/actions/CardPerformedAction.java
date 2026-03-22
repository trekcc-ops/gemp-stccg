package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface CardPerformedAction extends Action {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("performingCardId")
    PhysicalCard getPerformingCard();

}
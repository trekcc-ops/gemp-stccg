package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collection;

public interface SelectCardsAction extends Action {

    @JsonProperty("selectedCards")
    @JsonIdentityReference(alwaysAsId=true)
    Collection<PhysicalCard> getSelectedCards();

}
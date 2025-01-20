package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Collection;
import java.util.List;

public interface SelectCardAction extends SelectCardsAction {

    @JsonIgnore
    PhysicalCard getSelectedCard();

    default Collection<PhysicalCard> getSelectedCards() {
        return List.of(getSelectedCard());
    }
}
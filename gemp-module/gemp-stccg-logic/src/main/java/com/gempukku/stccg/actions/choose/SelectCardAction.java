package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collection;
import java.util.List;

public interface SelectCardAction extends SelectCardsAction {

    PhysicalCard getSelectedCard();

    default Collection<PhysicalCard> getSelectedCards() {
        return List.of(getSelectedCard());
    }
}
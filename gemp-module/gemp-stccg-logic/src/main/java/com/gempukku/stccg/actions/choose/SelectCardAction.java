package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import org.checkerframework.checker.units.qual.C;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface SelectCardAction extends SelectCardsAction {

    @JsonIgnore
    PhysicalCard getSelectedCard();

    default Collection<PhysicalCard> getSelectedCards() {
        List<PhysicalCard> result = new LinkedList<>();
        if (getSelectedCard() != null) {
            result.add(getSelectedCard());
        }
        return result;
    }

    default int getMinimum() {
        return 1;
    }

    default int getMaximum() {
        return 1;
    }
}
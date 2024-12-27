package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collection;

public interface SelectCardsAction extends Action {

    Collection<PhysicalCard> getSelectedCards();
}
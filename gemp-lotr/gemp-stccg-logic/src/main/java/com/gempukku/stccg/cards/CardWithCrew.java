package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Collection;

public interface CardWithCrew {
    Collection<PhysicalCard> getCrew();
    boolean isCompatibleWith(Affiliation affiliation);
}

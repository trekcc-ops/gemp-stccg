package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Collection;
import java.util.LinkedList;

public interface CardWithCrew {
    Collection<PhysicalCard> getCrew();
    boolean isCompatibleWith(Affiliation affiliation);

    default Collection<PersonnelCard> getPersonnelInCrew() {
        Collection<PersonnelCard> personnelInCrew = new LinkedList<>();
        for (PhysicalCard card : getCrew()) {
            if (card instanceof PersonnelCard personnel)
                personnelInCrew.add(personnel);
        }
        return personnelInCrew;
    }
}
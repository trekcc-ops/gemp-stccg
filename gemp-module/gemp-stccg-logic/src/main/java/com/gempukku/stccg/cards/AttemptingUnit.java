package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Collection;
import java.util.LinkedList;

public interface AttemptingUnit {
    boolean canAttemptMission(MissionCard mission);
    Collection<PersonnelCard> getAllPersonnel();
    default Collection<PersonnelCard> getAttemptingPersonnel() {
        // TODO - Does not include a check for infiltrators
        Collection<PersonnelCard> personnelAttempting = new LinkedList<>();
        for (PhysicalCard card : getAllPersonnel()) {
            if (card instanceof PersonnelCard personnel)
                if (!personnel.isStopped() && !personnel.isDisabled() && !personnel.isInStasis() &&
                        personnel.getAffiliation() != Affiliation.BORG)
                    personnelAttempting.add(personnel);
        }
        return personnelAttempting;
    }
}

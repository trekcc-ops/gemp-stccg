package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalMissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalPersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Collection;
import java.util.LinkedList;

public interface AttemptingEntity {
    boolean canAttemptMission(PhysicalMissionCard mission);
    Collection<PhysicalPersonnelCard> getAllPersonnel();
    default Collection<PhysicalPersonnelCard> getAttemptingPersonnel() {
        // TODO - Does not include a check for infiltrators
        Collection<PhysicalPersonnelCard> personnelAttempting = new LinkedList<>();
        for (PhysicalCard card : getAllPersonnel()) {
            if (card instanceof PhysicalPersonnelCard personnel)
                if (!personnel.isStopped() && !personnel.isDisabled() && !personnel.isInStasis() &&
                        personnel.getCurrentAffiliation() != Affiliation.BORG)
                    personnelAttempting.add(personnel);
        }
        return personnelAttempting;
    }
}

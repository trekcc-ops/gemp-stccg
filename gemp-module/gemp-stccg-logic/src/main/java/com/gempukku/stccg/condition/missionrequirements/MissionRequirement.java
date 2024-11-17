package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;

import java.util.Collection;

public interface MissionRequirement {

    boolean canBeMetBy(PersonnelCard personnel);

    boolean canBeMetBy(Collection<PersonnelCard> personnel);

    default boolean canBeMetBy(AttemptingUnit attemptingUnit) {
        return canBeMetBy(attemptingUnit.getAttemptingPersonnel());
    }
}
package com.gempukku.stccg.condition.missionrequirements;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.blueprints.MissionRequirementDeserializer;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;

import java.util.Collection;

@JsonDeserialize(using = MissionRequirementDeserializer.class)
public interface MissionRequirement {

    boolean canBeMetBy(PersonnelCard personnel);

    boolean canBeMetBy(Collection<PersonnelCard> personnel);

    default boolean canBeMetBy(AttemptingUnit attemptingUnit) {
        return canBeMetBy(attemptingUnit.getAttemptingPersonnel());
    }
}
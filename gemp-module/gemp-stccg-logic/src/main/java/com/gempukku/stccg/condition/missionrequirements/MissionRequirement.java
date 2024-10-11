package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;

import java.util.*;

public interface MissionRequirement {

    boolean canBeMetBy(PersonnelCard personnel);

    boolean canBeMetBy(Collection<PersonnelCard> personnel);
}
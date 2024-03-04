package com.gempukku.stccg.requirement.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PhysicalPersonnelCard;

import java.util.*;

public interface MissionRequirement {

    boolean canBeMetBy(PhysicalPersonnelCard personnel);

    boolean canBeMetBy(Collection<PhysicalPersonnelCard> personnel);
}
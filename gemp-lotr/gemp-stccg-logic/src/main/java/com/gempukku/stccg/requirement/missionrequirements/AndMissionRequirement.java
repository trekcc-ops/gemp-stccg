package com.gempukku.stccg.requirement.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PhysicalPersonnelCard;

import java.util.Collection;
import java.util.List;

public class AndMissionRequirement implements MissionRequirement {

    private final List<MissionRequirement> _requirements;
    public AndMissionRequirement(List<MissionRequirement> requirements) {
        _requirements = requirements;
    }
    @Override
    public boolean canBeMetBy(PhysicalPersonnelCard personnel) {
        return _requirements.stream().allMatch(requirement -> requirement.canBeMetBy(personnel));
    }

    @Override
    public boolean canBeMetBy(Collection<PhysicalPersonnelCard> personnel) {
        return _requirements.stream().allMatch(requirement -> requirement.canBeMetBy(personnel));
    }
}
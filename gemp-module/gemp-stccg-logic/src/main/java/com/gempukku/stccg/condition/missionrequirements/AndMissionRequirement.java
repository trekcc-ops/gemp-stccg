package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AndMissionRequirement implements MissionRequirement {

    private final List<MissionRequirement> _requirements;
    public AndMissionRequirement(List<MissionRequirement> requirements) {
        _requirements = requirements;
    }
    public AndMissionRequirement(MissionRequirement... requirements) {
        _requirements = new LinkedList<>();
        Collections.addAll(_requirements, requirements);
    }
    public AndMissionRequirement(SkillName ... skills) {
        _requirements = new LinkedList<>();
        for (SkillName skill : skills) {
            _requirements.add(new RegularSkillMissionRequirement(skill));
        }
    }
    @Override
    public boolean canBeMetBy(PersonnelCard personnel) {
        return _requirements.stream().allMatch(requirement -> requirement.canBeMetBy(personnel));
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel) {
        return _requirements.stream().allMatch(requirement -> requirement.canBeMetBy(personnel));
    }
}
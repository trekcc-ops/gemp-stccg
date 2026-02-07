package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class OrMissionRequirement implements MissionRequirement {

    private final List<MissionRequirement> _requirements;
    public OrMissionRequirement(List<MissionRequirement> requirements) {
        _requirements = requirements;
    }

    public OrMissionRequirement(MissionRequirement... requirements) {
        _requirements = new LinkedList<>();
        _requirements.addAll(Arrays.asList(requirements));
    }

    public OrMissionRequirement(SkillName... skills) {
        _requirements = new LinkedList<>();
        for (SkillName skill : skills) {
            _requirements.add(new RegularSkillMissionRequirement(skill));
        }
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        return _requirements.stream().anyMatch(requirement -> requirement.canBeMetBy(personnel, cardGame));
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" OR ");
        for (MissionRequirement requirement : _requirements) {
            sj.add(requirement.toString());
        }
        return sj.toString();
    }
}
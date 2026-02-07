package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

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
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        return _requirements.stream().allMatch(requirement -> requirement.canBeMetBy(personnel, cardGame));
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" + ");
        for (MissionRequirement requirement : _requirements) {
            if (requirement instanceof OrMissionRequirement orReq) {
                sj.add("(" + orReq + ")");
            } else {
                sj.add(requirement.toString());
            }
        }
        return sj.toString();
    }
}
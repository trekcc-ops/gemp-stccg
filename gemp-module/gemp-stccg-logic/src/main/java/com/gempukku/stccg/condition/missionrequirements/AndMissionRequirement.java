package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Lists;

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

    @Override
    public boolean requiresSkill(SkillName skillName) {
        for (MissionRequirement requirement : _requirements) {
            if (requirement.requiresSkill(skillName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<MissionRequirement> getRequirementOptionsWithoutOr() {
        List<List<MissionRequirement>> allRequirements = new ArrayList<>();
        for (MissionRequirement requirement : _requirements) {
            allRequirements.add(requirement.getRequirementOptionsWithoutOr());
        }
        List<List<MissionRequirement>> cartesian = Lists.cartesianProduct(allRequirements);
        List<MissionRequirement> result = new ArrayList<>();
        for (List<MissionRequirement> reqList : cartesian) {
            result.add(new AndMissionRequirement(reqList));
        }
        return result;
    }
}
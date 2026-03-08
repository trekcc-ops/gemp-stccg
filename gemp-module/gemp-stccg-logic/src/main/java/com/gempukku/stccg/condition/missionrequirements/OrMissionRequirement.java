package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.GameTextContext;
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
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context) {
        return _requirements.stream().anyMatch(requirement -> requirement.canBeMetBy(personnel, cardGame, context));
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" OR ");
        for (MissionRequirement requirement : _requirements) {
            sj.add(requirement.toString());
        }
        return sj.toString();
    }

    @Override
    public boolean requiresSkill(SkillName skillName, DefaultGame cardGame, GameTextContext context) {
        for (MissionRequirement req : _requirements) {
            if (!req.requiresSkill(skillName, cardGame, context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<MissionRequirement> getRequirementOptionsWithoutOr() {
        List<MissionRequirement> result = new ArrayList<>();
        for (MissionRequirement requirement : _requirements) {
            result.add(requirement);
        }
        return result;
    }
}
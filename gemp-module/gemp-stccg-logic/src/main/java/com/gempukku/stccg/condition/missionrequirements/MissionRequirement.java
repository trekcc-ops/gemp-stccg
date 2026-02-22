package com.gempukku.stccg.condition.missionrequirements;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.MissionRequirementDeserializer;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonDeserialize(using = MissionRequirementDeserializer.class)
public interface MissionRequirement {

    boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame);

    String toString();

    boolean requiresSkill(SkillName skillName);

    default List<MissionRequirement> getRequirementOptionsWithoutOr() {
        return List.of(this);
    }

    default boolean canBeMetWithMissionSpecialistHelping(Collection<PersonnelCard> allPersonnel, DefaultGame cardGame) {
        Map<SkillName, PersonnelCard> missionSpecMap = new HashMap<>();
        for (PersonnelCard personnel : allPersonnel) {
            if (Filters.missionSpecialist.accepts(cardGame, personnel)) {
                Skill skill = personnel.getSkills(cardGame).getFirst();
                if (skill instanceof RegularSkill regSkill) {
                    missionSpecMap.put(regSkill.getRegularSkill(), personnel);
                }
            }
        }
        if (missionSpecMap.isEmpty()) {
            return false;
        }

        List<MissionRequirement> requirementsWithoutOr = getRequirementOptionsWithoutOr();
        for (MissionRequirement req : requirementsWithoutOr) {
            if (req.canBeMetBy(allPersonnel, cardGame)) {
                for (SkillName skillName : missionSpecMap.keySet()) {
                    if (req.requiresSkill(skillName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
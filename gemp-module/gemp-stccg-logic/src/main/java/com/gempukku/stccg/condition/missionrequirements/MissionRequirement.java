package com.gempukku.stccg.condition.missionrequirements;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.GameTextContext;
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

    boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame, GameTextContext context);

    String toString();

    default boolean requiresSkill(SkillName skillName, DefaultGame cardGame, GameTextContext context) {
        return false;
    }


    default List<MissionRequirement> getRequirementOptionsWithoutOr() {
        return List.of(this);
    }

    default boolean canBeMetWithMissionSpecialistHelping(Collection<PersonnelCard> allPersonnel, DefaultGame cardGame,
                                                         GameTextContext context) {
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
            if (req.canBeMetBy(allPersonnel, cardGame, context)) {
                for (SkillName skillName : missionSpecMap.keySet()) {
                    if (req.requiresSkill(skillName, cardGame, context)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
package com.gempukku.stccg.condition.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class RegularSkillMissionRequirement implements MissionRequirement {

    private final SkillName _skill;
    private final int _count;
    public RegularSkillMissionRequirement(SkillName skill) {
        _skill = skill;
        _count = 1;
    }
    public RegularSkillMissionRequirement(SkillName skill, int count) {
        _skill = skill;
        _count = count;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel, DefaultGame cardGame) {
        int totalSkillLevel = 0;
        for (PersonnelCard card : personnel) {
            totalSkillLevel += card.getSkillLevel(cardGame, _skill);
        }
        return totalSkillLevel >= _count;
    }

    public String toString() {
        String result;
        result = _skill.get_humanReadable();
        if (_count > 1)
            result = result + " x" + _count;
        return result;
    }

}
package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

public class HasSkillFilter implements CardFilter {

    @JsonProperty("skill")
    private final SkillName _skill;

    private final int _level;

    public HasSkillFilter(SkillName skill) {
        _skill = skill;
        _level = 1;
    }

    public HasSkillFilter(SkillName skill, int level) {
        _skill = skill;
        _level = level;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof PersonnelCard personnel) {
            return personnel.getSkillLevel(game, _skill) >= _level;
        } else if (_level == 1) {
            return physicalCard.hasSkill(_skill, game);
        } else {
            return false;
        }
    }

    public boolean requiresSkill(SkillName skillName) {
        return _skill == skillName;
    }
}
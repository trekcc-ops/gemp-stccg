package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.RegularSkill;

public class Skill {
    private final RegularSkill _regularSkill;
    private final int _level;

    public Skill(RegularSkill skill, Integer level) {
        _regularSkill = skill;
        _level = level;
    }

    public RegularSkill getRegularSkill() { return _regularSkill; }
    public int getLevel() { return _level; }
}

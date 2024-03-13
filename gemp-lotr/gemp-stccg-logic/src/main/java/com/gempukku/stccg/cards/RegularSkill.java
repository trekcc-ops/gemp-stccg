package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.SkillType;

public class RegularSkill extends Skill {
    private final SkillName _SkillName;
    private final int _level;

    public RegularSkill(SkillName skill, Integer level) {
        super(SkillType.REGULAR);
        _SkillName = skill;
        _level = level;
    }

    public SkillName getRegularSkill() { return _SkillName; }
    public int getLevel() { return _level; }
}

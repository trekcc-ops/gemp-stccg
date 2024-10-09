package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.SkillType;

public class RegularSkill extends Skill {
    private final SkillName _SkillName;
    private final int _level;

    public RegularSkill(SkillName skill, Integer level) {
        super(SkillType.REGULAR, skill.get_humanReadable() + " x" + level);
        _SkillName = skill;
        _level = level;
    }

    public RegularSkill(SkillName skill) {
        this(skill, 1);
    }

    public SkillName getRegularSkill() { return _SkillName; }
    public int getLevel() { return _level; }
}

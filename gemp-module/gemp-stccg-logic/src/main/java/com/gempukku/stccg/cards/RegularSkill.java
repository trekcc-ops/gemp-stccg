package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.Objects;

public class RegularSkill extends Skill {
    private final SkillName _SkillName;
    private final int _level;

    @JsonCreator
    public RegularSkill(
            @JsonProperty("name")
            SkillName skill,
            @JsonProperty("level")
            Integer level) {
        super();
        _SkillName = skill;
        _level = Objects.requireNonNullElse(level, 1);
    }

    public RegularSkill(SkillName skill) {
        this(skill, 1);
    }

    public SkillName getRegularSkill() { return _SkillName; }
    public int getLevel() { return _level; }
}
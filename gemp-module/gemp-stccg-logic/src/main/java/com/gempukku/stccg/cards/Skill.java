package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.SkillType;

public class Skill {
    protected final SkillType _skillType;
    private final String _text;

    public Skill(SkillType skillType, String text) {
        _skillType = skillType;
        _text = text;
    }

    // TODO - This may need to be different for 1E vs. 2E
    public String getSkillText() { return _text; }
}

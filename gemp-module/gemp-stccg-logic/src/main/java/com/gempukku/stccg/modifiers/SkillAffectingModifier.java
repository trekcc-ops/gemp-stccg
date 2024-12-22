package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.SkillName;

import java.util.List;

public interface SkillAffectingModifier {
    List<SkillName> getSkills();
}
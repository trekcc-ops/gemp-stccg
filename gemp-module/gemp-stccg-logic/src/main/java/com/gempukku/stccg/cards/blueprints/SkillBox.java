package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.LinkedList;
import java.util.List;

@JsonDeserialize(using = SkillBoxDeserializer.class)
public class SkillBox {
    private int _skillDots;
    private final int _sdIcons;
    private final List<Skill> _skillList = new LinkedList<>();

    public SkillBox(int skillDots, int sdIcons, List<Skill> skills) {
        _skillDots = skillDots;
        _sdIcons = sdIcons;
        _skillList.addAll(skills);
    }

    public int getSkillDots() {
        return _skillDots;
    }

    public int getSdIcons() {
        return _sdIcons;
    }

    public List<Skill> getSkillList() {
        return _skillList;
    }

    public void addSkill(Skill skill) { _skillList.add(skill); }

    public void addSkill(SkillName skillName) { _skillList.add(new RegularSkill(skillName)); }

    public void setSkillDots(int skillDots) { _skillDots = skillDots; }

}
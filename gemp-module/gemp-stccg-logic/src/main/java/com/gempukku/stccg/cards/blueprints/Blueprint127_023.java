package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.common.filterable.SkillName;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint127_023 extends CardBlueprint {

    // Robert DeSoto
    Blueprint127_023() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.LEADERSHIP));
        skillList.add(new RegularSkill(SkillName.HONOR));
        skillList.add(new RegularSkill(SkillName.EXOBIOLOGY));
        _skillBox = new SkillBox(4,0,skillList);
    }

}
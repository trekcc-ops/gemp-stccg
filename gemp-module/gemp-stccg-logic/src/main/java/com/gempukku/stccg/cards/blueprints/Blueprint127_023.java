package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.NullSkill;
import com.gempukku.stccg.common.filterable.SkillName;


public class Blueprint127_023 extends CardBlueprint {

    // Robert DeSoto
    Blueprint127_023() {
        super("127_023");
        addSkill(new NullSkill());
        addSkill(SkillName.LEADERSHIP);
        addSkill(SkillName.HONOR);
        addSkill(SkillName.EXOBIOLOGY);
        setSkillDotIcons(4);
    }

}
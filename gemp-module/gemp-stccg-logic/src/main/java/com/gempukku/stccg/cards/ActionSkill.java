package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillType;

public abstract class ActionSkill extends Skill {
    public ActionSkill(String text) {
        super(SkillType.SPECIAL, text);
    }

    public abstract TopLevelSelectableAction getAction(PhysicalCard card);
}
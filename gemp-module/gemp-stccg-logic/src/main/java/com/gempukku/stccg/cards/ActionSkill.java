package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillType;

public abstract class ActionSkill extends Skill {
    private final String _text;

    public ActionSkill(String text) {
        super(SkillType.SPECIAL);
        _text = text;
    }

    public abstract Action getAction(PhysicalCard card);
    public String getSkillText() { return _text; }
}

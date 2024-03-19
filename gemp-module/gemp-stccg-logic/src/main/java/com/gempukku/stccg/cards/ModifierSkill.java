package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillType;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;

public abstract class ModifierSkill extends Skill {
    private final String _text;

    public ModifierSkill(String text) {
        super(SkillType.SPECIAL);
        _text = text;
    }

    public abstract Modifier getModifier(Player player, PhysicalCard card);
    public String getSkillText() { return _text; }
}

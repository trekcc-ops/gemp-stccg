package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillType;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;

public abstract class ModifierSkill extends Skill {
    public ModifierSkill(String text) {
        super(SkillType.SPECIAL, text);
    }

    public abstract Modifier getModifier(Player player, PhysicalCard card) throws InvalidGameLogicException;
}
package com.gempukku.stccg.cards.blueprints;


import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.FacingDilemmaCondition;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.LinkedList;
import java.util.List;

public class Blueprint155_061 extends CardBlueprint {

    // Kosinski

    Blueprint155_061() {
        super("155_061");
        setSkillDotIcons(5);
        addSkill(SkillName.COMPUTER_SKILL);
        addSkill(SkillName.PHYSICS);
        addSkill(SkillName.SCIENCE);
        addSkill(SkillName.STELLAR_CARTOGRAPHY);
        addSkill(specialSkill());
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard)
            throws InvalidGameLogicException {
        // TODO - Need some additional work here to check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : _skills)
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(player, thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("X=4 when facing a dilemma.") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) throws InvalidGameLogicException {
                return new CunningModifier(thisCard, thisCard, new FacingDilemmaCondition(thisCard), -4);
            }
        };
    }


}
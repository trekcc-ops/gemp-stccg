package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint106_014 extends CardBlueprint {
    Blueprint106_014() {
        super("106_014"); // Admiral McCoy
        addSkill(SkillName.MEDICAL);
        addSkill(SkillName.EXOBIOLOGY);
        addSkill(SkillName.CANTANKEROUSNESS);
        addSkill(specialSkill());
        setSkillDotIcons(4);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
            // TODO - Need some additional work here to be check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : _skills)
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(player, thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("Other MEDICAL are CUNNING +3 where present.") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                return new CunningModifier(thisCard,
                        Filters.and(Filters.otherCardPresentWith(thisCard), SkillName.MEDICAL, CardType.PERSONNEL), 3);
            }
        };
    }
}
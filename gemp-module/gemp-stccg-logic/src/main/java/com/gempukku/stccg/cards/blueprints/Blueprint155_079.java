package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint155_079 extends CardBlueprint {
    Blueprint155_079() {
        super("155_079");
        addSkill(SkillName.OFFICER);
        addSkill(SkillName.DIPLOMACY);
        addSkill(SkillName.NAVIGATION);
        addSkill(SkillName.HONOR, 2);
        addSkill(specialSkill());
        setSkillDotIcons(5);
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
        return new ModifierSkill("If with any K'Ehleyr, both are attributes all +2.") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                Filterable usageFilter = Filters.any(Characteristic.K_EHLEYR);
                Filterable affectFilter = Filters.or(thisCard,
                        Filters.and(Filters.presentWith(thisCard), Characteristic.K_EHLEYR));
                return new AllAttributeModifier(thisCard, affectFilter,
                        new PresentWithYourCardCondition(thisCard, usageFilter), 2);
            }
        };
    }
}
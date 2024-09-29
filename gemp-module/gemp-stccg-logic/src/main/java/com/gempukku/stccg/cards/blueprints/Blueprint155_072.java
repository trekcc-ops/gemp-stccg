package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint155_072 extends CardBlueprint {
    Blueprint155_072() {
        // Kol (The Next Generation)
        super("155_072");
        addSkill(SkillName.DIPLOMACY);
        addSkill(SkillName.NAVIGATION);
        addSkill(SkillName.GREED);
        addSkill(SkillName.STELLAR_CARTOGRAPHY);
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
        return new ModifierSkill("Attributes all +2 if with Goss or Dr. Arridor.") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                Filterable usageFilter = Filters.or(Filters.name("Goss"), Filters.name("Dr. Arridor"));
                return new AllAttributeModifier(thisCard, thisCard,
                        new PresentWithYourCardCondition(thisCard, usageFilter), 2);
            }
        };
    }
}
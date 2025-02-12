package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Blueprint155_072 extends CardBlueprint {
    // Kol (The Next Generation)
    Blueprint155_072() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.DIPLOMACY));
        skillList.add(new RegularSkill(SkillName.NAVIGATION));
        skillList.add(new RegularSkill(SkillName.GREED));
        skillList.add(new RegularSkill(SkillName.STELLAR_CARTOGRAPHY));
        skillList.add(specialSkill());
        _skillBox = new SkillBox(5,0,skillList);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard)
            throws InvalidGameLogicException {
            // TODO - Need some additional work here to be check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : getSkills(thisCard.getGame(), thisCard))
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("Attributes all +2 if with Goss or Dr. Arridor.") {
            @Override
            public Modifier getModifier(PhysicalCard thisCard) {
                Filterable usageFilter = Filters.or(Filters.name("Goss"), Filters.name("Dr. Arridor"));
                return new AllAttributeModifier(thisCard, thisCard,
                        new PresentWithYourCardCondition(thisCard, usageFilter), 2);
            }
        };
    }
}
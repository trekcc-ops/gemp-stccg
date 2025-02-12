package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
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


public class Blueprint155_079 extends CardBlueprint {
    Blueprint155_079() {
        super("155_079"); // Captain Worf (The Next Generation)
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.OFFICER));
        skillList.add(new RegularSkill(SkillName.DIPLOMACY));
        skillList.add(new RegularSkill(SkillName.NAVIGATION));
        skillList.add(new RegularSkill(SkillName.HONOR, 2));
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
        return new ModifierSkill("If with any K'Ehleyr, both are attributes all +2.") {
            @Override
            public Modifier getModifier(PhysicalCard thisCard) {
                Filterable usageFilter = Filters.any(Characteristic.K_EHLEYR);
                Filterable affectFilter = Filters.or(thisCard,
                        Filters.and(Filters.presentWith(thisCard), Characteristic.K_EHLEYR));
                return new AllAttributeModifier(thisCard, affectFilter,
                        new PresentWithYourCardCondition(thisCard, usageFilter), 2);
            }
        };
    }
}
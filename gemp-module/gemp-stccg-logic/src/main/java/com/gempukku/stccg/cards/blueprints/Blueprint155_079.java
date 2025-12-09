package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.CharacteristicFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierTimingType;
import com.gempukku.stccg.modifiers.attributes.Modifiers;
import com.gempukku.stccg.requirement.PresentWithYourCardCondition;

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
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame, PhysicalCard thisCard)
            throws InvalidGameLogicException {
            // TODO - Need some additional work here to be check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : getSkills(cardGame, thisCard))
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("If with any K'Ehleyr, both are attributes all +2.") {
            @Override
            public Modifier getModifier(PhysicalCard thisCard) {
                Filterable usageFilter = new CharacteristicFilter(Characteristic.K_EHLEYR);
                CardFilter affectFilter = Filters.or(Filters.card(thisCard),
                        Filters.and(Filters.presentWith(thisCard), Characteristic.K_EHLEYR));
                return Modifiers.allPersonnelAttributes(thisCard, affectFilter,
                        new PresentWithYourCardCondition(thisCard, usageFilter), 2,
                        ModifierTimingType.WHILE_IN_PLAY);
            }
        };
    }
}
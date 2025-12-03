package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Species;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("unused")
public class Blueprint112_167 extends CardBlueprint {
    Blueprint112_167() {
        super("112_167"); // Shakaar Edon
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.SECURITY));
        skillList.add(new RegularSkill(SkillName.RESISTANCE, 2));
        skillList.add(new RegularSkill(SkillName.LEADERSHIP));
        skillList.add(new RegularSkill(SkillName.GEOLOGY));
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
        return new ModifierSkill("Other Bajorans present are STRENGTH +2.") {
            @Override
            public Modifier getModifier(PhysicalCard thisCard) {
                return new StrengthModifier(thisCard, Filters.and(
                        Filters.otherCardPresentWith(thisCard),
                        CardType.PERSONNEL, Filters.or(Species.BAJORAN, Affiliation.BAJORAN)), 2);
            }
        };
    }
}
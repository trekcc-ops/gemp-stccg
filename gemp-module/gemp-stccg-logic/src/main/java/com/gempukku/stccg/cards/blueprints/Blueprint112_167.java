package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("unused")
public class Blueprint112_167 extends CardBlueprint {
    Blueprint112_167() {
        super("112_167"); // Shakaar Edon
        addSkill(SkillName.SECURITY);
        addSkill(SkillName.RESISTANCE, 2);
        addSkill(SkillName.LEADERSHIP);
        addSkill(SkillName.GEOLOGY);
        addSkill(specialSkill());
        setSkillDotIcons(5);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard)
            throws InvalidGameLogicException {
            // TODO - Need some additional work here to be check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : _skills)
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(player, thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("Other Bajorans present are STRENGTH +2.") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                return new StrengthModifier(thisCard, Filters.and(
                        Filters.otherCardPresentWith(thisCard),
                        CardType.PERSONNEL, Filters.or(Species.BAJORAN, Affiliation.BAJORAN)), 2);
            }
        };
    }
}
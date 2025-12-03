package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Blueprint106_014 extends CardBlueprint {

    // Admiral McCoy
    Blueprint106_014() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.MEDICAL));
        skillList.add(new RegularSkill(SkillName.EXOBIOLOGY));
        skillList.add(new RegularSkill(SkillName.CANTANKEROUSNESS));
        skillList.add(specialSkill());
        _skillBox = new SkillBox(4,0,skillList);
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
        return new ModifierSkill("Other MEDICAL are CUNNING +3 where present.") {
            @Override
            public Modifier getModifier(PhysicalCard thisCard) {
                return new CunningModifier(thisCard,
                        Filters.and(Filters.otherCardPresentWith(thisCard), SkillName.MEDICAL, CardType.PERSONNEL), 3);
            }
        };
    }
}
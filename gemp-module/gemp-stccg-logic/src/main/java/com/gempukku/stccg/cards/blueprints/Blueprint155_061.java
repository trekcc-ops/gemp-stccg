package com.gempukku.stccg.cards.blueprints;


import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;
import com.gempukku.stccg.requirement.FacingDilemmaCondition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_061 extends CardBlueprint {

    // Kosinski

    Blueprint155_061() {
        List<Skill> skillList = new ArrayList<>();
        skillList.add(new RegularSkill(SkillName.COMPUTER_SKILL));
        skillList.add(new RegularSkill(SkillName.PHYSICS));
        skillList.add(new RegularSkill(SkillName.SCIENCE));
        skillList.add(new RegularSkill(SkillName.STELLAR_CARTOGRAPHY));
        skillList.add(specialSkill());
        _skillBox = new SkillBox(5,0,skillList);
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame, PhysicalCard thisCard)
            throws InvalidGameLogicException {
        // TODO - Need some additional work here to check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : getSkills(cardGame, thisCard))
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifierOld(thisCard));
        return modifiers;
    }

    private ModifierSkill specialSkill() {
        return new ModifierSkill("X=4 when facing a dilemma.") {
            @Override
            public Modifier getModifierOld(PhysicalCard thisCard) throws InvalidGameLogicException {
                return new CunningModifier(thisCard, Filters.card(thisCard), new FacingDilemmaCondition(thisCard), -4);
            }
        };
    }


}
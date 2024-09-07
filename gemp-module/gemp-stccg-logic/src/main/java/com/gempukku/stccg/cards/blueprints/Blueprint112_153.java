package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint112_153 extends CardBlueprint {
    Blueprint112_153() {
        super("112_153");
        setTitle("Kira Nerys");
        addAffiliation(Affiliation.BAJORAN);
        setCardType(CardType.PERSONNEL);
        setPropertyLogo(PropertyLogo.DS9_LOGO);
        setClassification(SkillName.OFFICER);
        addIcons(CardIcon.COMMAND, CardIcon.ORB);
        setLore("Outspoken Major in Bajoran Militia. Assigned as first officer of Deep Space 9. Former member of Shakaar resistance cell. Romantically involved with Odo.");
        addSkill(SkillName.LEADERSHIP);
        addSkill(SkillName.RESISTANCE);
        addSkill(SkillName.SECURITY);
        addSkill(SkillName.NAVIGATION, 2);
        addSkill(SkillName.COMPUTER_SKILL);
        addSkill(specialSkill());
        setSkillDotIcons(6);
        setAttribute(CardAttribute.INTEGRITY, 7);
        setAttribute(CardAttribute.CUNNING, 7);
        setAttribute(CardAttribute.STRENGTH, 8);
        setImageUrl("https://www.trekcc.org/1e/cardimages/vpromos/211VP.jpg");
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
        return new ModifierSkill("X=3 vs. " + Affiliation.CARDASSIAN.toHTML() + ".") {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                    // TODO - Need to get clarity on what "vs. Cardassian" means
                return new StrengthModifier(thisCard, new TrueCondition(), 3);
            }
        };
    }
}
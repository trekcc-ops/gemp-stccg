package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ModifierSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint116_054 extends CardBlueprint {
    Blueprint116_054() {
        super("116_054");
        setTitle("Riker Wil");
        addAffiliation(Affiliation.BAJORAN);
        setCardType(CardType.PERSONNEL);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setClassification(SkillName.SECURITY);
        addIcons(CardIcon.STAFF, CardIcon.MAQUIS);
        setLore("In 2370, the human William T. Riker accompanied Ro Laren on a clandestine mission to investigate the Maquis. He posed as a Bajoran freedom fighter.");
            // TODO - Implement personas
//        addPersona("William T. Riker");
        addSkill(SkillName.NAVIGATION);
        addSkill(SkillName.TREACHERY);
        addSkill(SkillName.DIPLOMACY);
        addSkill(SkillName.MUSIC);
            // TODO - Implement icons in gametext
                // TODO - This special skill currently not implemented at all
//        addSkill(specialSkill("While at your [Fed] mission, adds [Baj]."));
        setSkillDotIcons(5);
        setAttribute(CardAttribute.INTEGRITY, 5);
        setAttribute(CardAttribute.CUNNING, 8);
        setAttribute(CardAttribute.STRENGTH, 8);
        setImageUrl("https://www.trekcc.org/1e/cardimages/bog/rikerwil.gif");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
            // TODO - Need some additional work here to check skill for usability
        List<Modifier> modifiers = new LinkedList<>();
        for (Skill skill : _skills)
            if (skill instanceof ModifierSkill modifierSkill)
                modifiers.add(modifierSkill.getModifier(player, thisCard));
        return modifiers;
    }
            // TODO - Need to create infrastructure for affiliation icon modifiers
/*
    private ModifierSkill specialSkill(String text) {
        return new ModifierSkill(text) {
            @Override
            public Modifier getModifier(Player player, final PhysicalCard thisCard) {
                return new AddAffiliationIconToMissionModifier(thisCard,
                        new AtMissionWithAffiliationIconCondition(thisCard),
                        Affiliation.BAJORAN);
            }
        };
    } */
}
package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.PropertyLogo;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint101_065 extends CardBlueprint {
    Blueprint101_065() {
        super("101_065");
        setTitle("Tricorder");
        setCardType(CardType.EQUIPMENT);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setLore("Representative of a multipurpose handheld device combining sensors, computers, and recorders. Such equipment has been developed by many races.");
//        setGameText("Gives all of your ENGINEER-classification personnel the extra skill of SCIENCE where present.");
        setImageUrl("https://www.trekcc.org/1e/cardimages/premiere/tricorder95.jpg");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL,
                Filters.classification(SkillName.ENGINEER));
        modifiers.add(new GainSkillModifier(thisCard, affectFilter, new TrueCondition(), SkillName.SCIENCE));
        return modifiers;
    }
}
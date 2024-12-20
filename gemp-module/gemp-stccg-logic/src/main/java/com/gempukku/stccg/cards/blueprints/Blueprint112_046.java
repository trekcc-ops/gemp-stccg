package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint112_046 extends CardBlueprint {
    Blueprint112_046() {
        super("112_046"); // Bajoran Phaser
//        setGameText("Bajoran and Non-Aligned use only. Each of your personnel present is STRENGTH +2. (Cumulative.)");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.or(Affiliation.NON_ALIGNED, Affiliation.BAJORAN, Species.BAJORAN));
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL);
        modifiers.add(new StrengthModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2));
        return modifiers;
    }
}
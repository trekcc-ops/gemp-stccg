package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint101_062 extends CardBlueprint {
    Blueprint101_062() {
        super("101_062"); // Romulan Disruptor
        setLore("Directed-energy weapon used by Romulans and other races. Disruptor fire can be identified by a high residue of antiprotons that linger for several hours.");
//        setGameText("Romulan and Non-Aligned use only. Each of your personnel STRENGTH +2 where present. (Cumulative.)");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.or(Affiliation.NON_ALIGNED, Filters.Romulan));
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWithThisCard(thisCard), CardType.PERSONNEL);
        modifiers.add(new StrengthModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2));
        return modifiers;
    }
}
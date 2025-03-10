package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.LinkedList;
import java.util.List;

public class Blueprint101_059 extends CardBlueprint {
    Blueprint101_059() {
        super("101_059");// Klingon PADD
        setLore("Standard Klingon Personal Access Display Device for computerized information.");
//        setGameText("Klingon use only. Each of your personnel CUNNING +2 where present. (Cumulative.)");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.Klingon);
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWithThisCard(thisCard), CardType.PERSONNEL);
        modifiers.add(new CunningModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2, true));
        return modifiers;
    }
}
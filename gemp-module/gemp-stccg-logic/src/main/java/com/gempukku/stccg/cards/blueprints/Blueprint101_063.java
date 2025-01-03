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

public class Blueprint101_063 extends CardBlueprint {
    Blueprint101_063() {
        super("101_063"); // Romulan PADD
        setLore("Standard Romulan Personal Access Display Device for computerized information.");
//        setGameText("Romulan use only. Each of your personnel CUNNING +2 where present. (Cumulative.)");
        setImageUrl("https://www.trekcc.org/1e/cardimages/premiere/romulanpadd95.jpg");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.Romulan);
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWithThisCard(thisCard), CardType.PERSONNEL);
        modifiers.add(new CunningModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2, true));
        return modifiers;
    }
}
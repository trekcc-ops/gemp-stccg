package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.player.YouPlayerResolver;
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


public class Blueprint101_058 extends CardBlueprint {
    // Klingon Disruptor
    Blueprint101_058() {
        super("101_058");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        YouPlayerResolver you = new YouPlayerResolver(thisCard);
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.or(Affiliation.NON_ALIGNED, Filters.Klingon));
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(you, thisCard), CardType.PERSONNEL);
        modifiers.add(new StrengthModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2));
        return modifiers;
    }
}
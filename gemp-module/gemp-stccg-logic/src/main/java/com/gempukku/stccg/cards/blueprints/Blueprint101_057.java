package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_057 extends CardBlueprint {
    Blueprint101_057() {
        super("101_057"); // Federation PADD
        setLore("Standard Federation Personal Access Display Device for computerized information.");
//        setGameText("Federation use only. Each of your personnel CUNNING +2 where present. (Cumulative.)");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Affiliation.FEDERATION);
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL);
        modifiers.add(new CunningModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2, true));
        return modifiers;
    }
}
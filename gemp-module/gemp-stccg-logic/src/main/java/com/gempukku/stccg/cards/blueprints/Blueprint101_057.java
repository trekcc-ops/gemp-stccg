package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.PropertyLogo;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.CunningModifier;

import java.util.LinkedList;
import java.util.List;

public class Blueprint101_057 extends CardBlueprint {
    Blueprint101_057() {
        super("101_057");
        setTitle("Federation PADD");
        setCardType(CardType.EQUIPMENT);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setLore("Standard Federation Personal Access Display Device for computerized information.");
//        setGameText("Federation use only. Each of your personnel CUNNING +2 where present. (Cumulative.)");
        setImageUrl("https://www.trekcc.org/1e/cardimages/premiere/federationpadd95.jpg");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Affiliation.FEDERATION);
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL);
        modifiers.add(new CunningModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2, true));
        return modifiers;
    }
}
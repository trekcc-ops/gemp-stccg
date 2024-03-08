package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint101_058 extends CardBlueprint {
    Blueprint101_058() {
        super("101_058");
        setTitle("Klingon Disruptor");
        setCardType(CardType.EQUIPMENT);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setLore("Phase-disruptor used by Klingons and other races. Similar to a phaser.");
//        setGameText("Klingon and Non-Aligned use only. Each of your personnel STRENGTH +2 where present. (Cumulative.)");
        setImageUrl("https://www.trekcc.org/1e/cardimages/premiere/klingondisruptor95.jpg");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable usageFilter = Filters.and(CardType.PERSONNEL, Filters.or(Affiliation.NON_ALIGNED, Affiliation.KLINGON, Species.KLINGON));
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL);
        modifiers.add(new StrengthModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard, usageFilter), 2, true));
        return modifiers;
    }
}
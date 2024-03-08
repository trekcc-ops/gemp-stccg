package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.PropertyLogo;
import com.gempukku.stccg.condition.PresentWithYourCardCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint101_064 extends CardBlueprint {
    Blueprint101_064() {
        super("101_064");
        setTitle("Starfleet Type II Phaser");
        setCardType(CardType.EQUIPMENT);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setLore("Handheld weapon can be set for stun, heat and disruption. PHASER is an acronym for PHASed Energy Rectification.");
//        setGameText("Federation and Non-Aligned use only. Each of your personnel present is STRENGTH +2. (Cumulative.)");
        setImageUrl("https://www.trekcc.org/1e/cardimages/errata/Starfleet-Type-II-Phaser.jpg");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL);
        modifiers.add(new StrengthModifier(thisCard, affectFilter,
                new PresentWithYourCardCondition(thisCard,
                        Filters.and(CardType.PERSONNEL, Filters.or(Affiliation.NON_ALIGNED, Affiliation.FEDERATION))),
                2, true));
                                // TODO - Does "Federation" have a different meaning here?
        return modifiers;
    }
}
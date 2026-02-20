package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;
import com.gempukku.stccg.modifiers.attributes.RangeModifier;
import com.gempukku.stccg.requirement.CardInPlayCondition;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.ThisCardAtMissionCondition;

import java.util.LinkedList;
import java.util.List;


public class Blueprint017_087 extends CardBlueprint {

    // Orion Interceptor
    Blueprint017_087() {
        super("017_087");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame,
                                                                           PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();

        // While you command another ship that has a cost of 6 or more, this ship is Range +2.
        CardFilter highCostShips = Filters.yourOtherCards(thisCard, CardType.SHIP,
                new CostFilter(6, ComparatorType.GREATER_THAN_OR_EQUAL_TO));
        Condition condition = new CardInPlayCondition(highCostShips);
        modifiers.add(new RangeModifier(thisCard, thisCard, condition,2));

        // While this ship is at a [NA] mission or a mission worth 40 or more points, it is Weapons +4 and Shields +4.
        CardFilter locationFilter = Filters.or(
                new MissionAffiliationIconFilter(Affiliation.NON_ALIGNED, thisCard.getOwnerName()),
                new MissionPointValueFilter(40, ComparatorType.GREATER_THAN_OR_EQUAL_TO));
        Condition locationCondition = new ThisCardAtMissionCondition(thisCard, locationFilter);
        modifiers.add(new AttributeModifier(thisCard, thisCard, locationCondition, 4,
                ModifierEffect.ATTRIBUTE_MODIFIER, CardAttribute.WEAPONS, CardAttribute.SHIELDS));
        return modifiers;
    }

}
package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.MultiplyEvaluator;
import com.gempukku.stccg.evaluator.ShieldsEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.DockedAtFilter;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.Objects;

public class DockingRules {
    private final static float HALF = 0.5f;

    // create a modifier that is turned on when a facility is played

    public static Modifier getExtendedShieldsModifier(FacilityCard dockedAtFacility) {
        CardFilter dockedAtCards = new DockedAtFilter(dockedAtFacility);
        Evaluator facilityShieldsEvaluator = new ShieldsEvaluator(dockedAtFacility);
        float multiplier = Objects.requireNonNullElse(
                dockedAtFacility.getBlueprint().getExtendedShieldsPercentage(), HALF);
        Evaluator halfOfShieldsEvaluator = new MultiplyEvaluator(multiplier, facilityShieldsEvaluator);
        return new AttributeModifier(dockedAtFacility, dockedAtCards, new TrueCondition(), halfOfShieldsEvaluator,
                CardAttribute.SHIELDS, ModifierEffect.ATTRIBUTE_MODIFIER);
    }

}
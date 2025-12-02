package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.MultiplyEvaluator;
import com.gempukku.stccg.evaluator.ShieldsEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.DockedAtFilter;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.ShieldsModifier;

public class DockingRules {
    private final static float HALF = 0.5f;

    // create a modifier that is turned on when a facility is played

    public static Modifier getExtendedShieldsModifier(FacilityCard dockedAtFacility) {
        CardFilter dockedAtCards = new DockedAtFilter(dockedAtFacility);
        Evaluator facilityShieldsEvaluator = new ShieldsEvaluator(dockedAtFacility);
        Evaluator halfOfShieldsEvaluator = new MultiplyEvaluator(HALF, facilityShieldsEvaluator);
        return new ShieldsModifier(dockedAtFacility, dockedAtCards, halfOfShieldsEvaluator);
    }

}
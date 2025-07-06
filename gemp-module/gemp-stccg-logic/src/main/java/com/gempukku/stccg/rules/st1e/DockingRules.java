package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.ShieldsEvaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.ShieldsModifier;

public class DockingRules {

    // create a modifier that is turned on when a facility is played

    public Modifier getExtendedShieldsModifier(FacilityCard dockedAtFacility) {

        // TODO - Need to fix some evaluator stuff
        CardFilter dockedAtCards = Filters.dockedAt(dockedAtFacility);
        Evaluator facilityShieldsEvaluator = new ShieldsEvaluator(dockedAtFacility); // TODO - Need to get 50%
        return new ShieldsModifier(dockedAtFacility, dockedAtCards, facilityShieldsEvaluator);
    }

}
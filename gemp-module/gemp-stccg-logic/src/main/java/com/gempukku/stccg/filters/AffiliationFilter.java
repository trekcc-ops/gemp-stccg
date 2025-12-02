package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;

public class AffiliationFilter implements CardFilter {

    private final Affiliation _affiliation;

    public AffiliationFilter(Affiliation affiliation) {
        _affiliation = affiliation;
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof AffiliatedCard noun)
            return noun.isAffiliation(_affiliation);
        else return false;
    }
}
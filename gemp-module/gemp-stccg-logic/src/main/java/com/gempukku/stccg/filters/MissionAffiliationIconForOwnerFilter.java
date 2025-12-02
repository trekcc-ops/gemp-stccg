package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;

public class MissionAffiliationIconForOwnerFilter implements CardFilter {

    private final Affiliation _affiliation;

    public MissionAffiliationIconForOwnerFilter(Affiliation affiliation) {
        _affiliation = affiliation;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof MissionCard missionCard &&
                missionCard.hasAffiliationIconForOwner(_affiliation);
    }
}
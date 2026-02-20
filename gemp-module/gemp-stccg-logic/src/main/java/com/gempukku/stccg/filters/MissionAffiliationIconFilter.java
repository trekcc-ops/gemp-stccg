package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;

public class MissionAffiliationIconFilter implements CardFilter {

    @JsonProperty("affiliation")
    private final Affiliation _affiliation;

    private final String _requestingPlayerName;

    public MissionAffiliationIconFilter(Affiliation affiliation,
                                        String requestingPlayerName) {
        _affiliation = affiliation;
        _requestingPlayerName = requestingPlayerName;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof MissionCard missionCard &&
                missionCard.hasAffiliation(game, _affiliation, _requestingPlayerName);
    }
}
package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collection;

public class FacilityEngineerRequirementLocationFilter implements CardFilter {

    private final CardFilter _engineerFilter;

    public FacilityEngineerRequirementLocationFilter(CardFilter engineerFilter) {
        _engineerFilter = engineerFilter;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof MissionCard mission && game instanceof ST1EGame stGame) {
            Collection<PhysicalCard> cardsAtLocation =
                    Filters.filterCardsInPlay(game, Filters.atLocation(mission.getGameLocation(stGame)));
            for (PhysicalCard cardAtLocation : cardsAtLocation) {
                if (_engineerFilter.accepts(game, cardAtLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

}
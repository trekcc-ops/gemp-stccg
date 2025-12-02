package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class YouControlAMatchingOutpostFilter implements CardFilter {

    private final String _playerName;
    public YouControlAMatchingOutpostFilter(Player player) {
        _playerName = player.getPlayerId();
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        for (PhysicalCard outpostCard : Filters.filterCardsInPlay(game, FacilityType.OUTPOST)) {
            if (outpostCard instanceof FacilityCard outpost && outpost.isControlledBy(_playerName) &&
                    physicalCard instanceof AffiliatedCard affiliatedCard &&
                    affiliatedCard.matchesAffiliationOf(outpost)) {
                return true;
            }
        }
        return false;
    }
}
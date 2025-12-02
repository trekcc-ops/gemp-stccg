package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameLocation;

public class AtLocationFilter implements CardFilter {

    private final int _locationId;

    public AtLocationFilter(GameLocation location) {
        _locationId = location.getLocationId();
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _locationId >= 0 && physicalCard.getGameLocation().getLocationId() == _locationId;
    }
}
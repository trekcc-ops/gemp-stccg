package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameLocation;

public class AtLocationFilter implements CardFilter {

    private final GameLocation _location;

    public AtLocationFilter(GameLocation location) {
        _location = location;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getGameLocation() == _location;
    }
}
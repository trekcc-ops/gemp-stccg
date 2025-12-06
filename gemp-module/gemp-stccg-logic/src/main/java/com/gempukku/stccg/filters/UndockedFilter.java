package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class UndockedFilter implements CardFilter {

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof ShipCard ship) {
            return !ship.isDocked();
        } else {
            return true;
        }
    }
}
package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class UndockedFilter implements CardFilter {

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (physicalCard instanceof PhysicalShipCard ship) {
            return !ship.isDocked();
        } else {
            return true;
        }
    }
}
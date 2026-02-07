package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class ExposedShipFilter implements CardFilter {

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof ShipCard ship && ship.isExposed();
    }
}
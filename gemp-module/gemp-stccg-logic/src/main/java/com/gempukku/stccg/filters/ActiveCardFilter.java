package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ActiveCardFilter implements CardFilter {
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isActive();
    }
}
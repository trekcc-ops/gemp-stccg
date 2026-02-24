package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.StoppableCard;
import com.gempukku.stccg.game.DefaultGame;

public class IsStoppedFilter implements CardFilter {
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof StoppableCard stoppable && stoppable.isStopped();
    }
}
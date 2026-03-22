package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsAboardFilter implements CardFilter {

    private final PhysicalCard _thisCard;

    public ThisCardIsAboardFilter(PhysicalCard thisCard) {
        _thisCard = thisCard;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _thisCard.isAboard(physicalCard);
    }
}
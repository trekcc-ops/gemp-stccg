package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PresentWithCardFilter implements CardFilter {

    private final PhysicalCard _card;

    public PresentWithCardFilter(PhysicalCard card) {
        _card = card;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isPresentWith(_card);
    }
}
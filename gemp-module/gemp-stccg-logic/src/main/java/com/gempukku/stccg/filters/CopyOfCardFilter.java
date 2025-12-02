package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CopyOfCardFilter implements CardFilter {

    private final PhysicalCard _originalCard;

    public CopyOfCardFilter(PhysicalCard originalCard) {
        _originalCard = originalCard;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isCopyOf(_originalCard);
    }
}
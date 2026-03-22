package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.game.DefaultGame;

public class SpecialDownloadIconCountFilter implements CardFilter {

    private final int _count;
    private final ComparatorType _comparator;

    public SpecialDownloadIconCountFilter(int count, ComparatorType comparator) {
        _count = count;
        _comparator = comparator;
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _comparator.isTrue(physicalCard.getBlueprint().getSpecialDownloadIconCount(), _count);
    }

}
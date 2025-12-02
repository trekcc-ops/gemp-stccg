package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SpecialDownloadIconsEqualTo implements CardFilter {

    private final int _count;

    public SpecialDownloadIconsEqualTo(int count) {
        _count = count;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getSpecialDownloadIconCount() == _count;
    }

}
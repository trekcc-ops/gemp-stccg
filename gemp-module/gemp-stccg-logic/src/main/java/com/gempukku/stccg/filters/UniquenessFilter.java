package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Uniqueness;
import com.gempukku.stccg.game.DefaultGame;

public class UniquenessFilter implements CardFilter {

    private final Uniqueness _uniqueness;

    public UniquenessFilter(Uniqueness uniqueness) {
        _uniqueness = uniqueness;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getUniqueness() == _uniqueness;
    }
}
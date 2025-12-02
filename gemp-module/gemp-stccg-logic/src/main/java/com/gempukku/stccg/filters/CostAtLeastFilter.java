package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CostAtLeastFilter implements CardFilter {

    private final int _cost;

    public CostAtLeastFilter(int cost) {
        _cost = cost;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getCost() >= _cost;
    }
}
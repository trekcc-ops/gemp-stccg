package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.game.DefaultGame;

public class CostFilter implements CardFilter {

    @JsonProperty("cost")
    private final int _cost;

    @JsonProperty("comparator")
    private final ComparatorType _comparator;

    @JsonCreator
    public CostFilter(@JsonProperty("cost") int cost, @JsonProperty("comparator") ComparatorType comparator) {
        _cost = cost;
        _comparator = comparator;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return _comparator.isTrue(physicalCard.getCost(), _cost);
    }
}
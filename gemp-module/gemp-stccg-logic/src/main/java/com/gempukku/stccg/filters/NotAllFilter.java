package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

public class NotAllFilter implements CardFilter {

    private final Filterable[] _filterables;

    public NotAllFilter(Filterable... filterables) {
        _filterables = filterables;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        CardFilter andFilter = new AndFilter(_filterables);
        return !andFilter.accepts(game, physicalCard);
    }
}
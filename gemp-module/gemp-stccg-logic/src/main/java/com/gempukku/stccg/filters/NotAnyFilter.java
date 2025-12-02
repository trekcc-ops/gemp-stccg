package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

public class NotAnyFilter implements CardFilter {

    private final Filterable[] _filterables;

    public NotAnyFilter(Filterable... filterables) {
        _filterables = filterables;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        CardFilter orFilter = new OrCardFilter(_filterables);
        return !orFilter.accepts(game, physicalCard);
    }
}
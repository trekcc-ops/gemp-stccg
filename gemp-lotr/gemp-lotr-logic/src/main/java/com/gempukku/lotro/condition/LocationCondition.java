package com.gempukku.lotro.condition;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class LocationCondition implements Condition {
    private final Filter _filter;

    public LocationCondition(Filterable... filters) {
        _filter = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return _filter.accepts(game, game.getGameState().getCurrentSite());
    }
}

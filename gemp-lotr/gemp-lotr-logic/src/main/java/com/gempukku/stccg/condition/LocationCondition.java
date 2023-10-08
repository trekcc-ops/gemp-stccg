package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

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

package com.gempukku.lotro.condition;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class CantSpotCondition implements Condition {
    private final Filterable[] _filters;

    public CantSpotCondition(Filterable... filters) {
        this._filters = filters;
    }

    @Override
    public boolean isFullfilled(DefaultGame game) {
        return !Filters.canSpot(game, _filters);
    }
}

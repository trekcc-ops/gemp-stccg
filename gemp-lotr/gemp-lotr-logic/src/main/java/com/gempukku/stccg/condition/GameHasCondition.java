package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class GameHasCondition implements Condition {
    private final Filterable[] _filter;
    private final int _count;

    public GameHasCondition(Filterable... filter) {
        this(1, Filters.and(filter));
    }

    public GameHasCondition(int count, Filterable... filter) {
        _filter = filter;
        _count = count;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return Filters.countActive(game, _filter)>=_count;
    }
}

package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class GameHasCondition implements Condition {
    private final Filterable[] _filter;
    private final int _count;
    private final DefaultGame _game;

    public GameHasCondition(ActionContext context, Filterable... filter) {
        this(context, 1, Filters.and(filter));
    }

    public GameHasCondition(ActionContext context, int count, Filterable... filter) {
        _filter = filter;
        _count = count;
        _game = context.getGame();
    }

    @Override
    public boolean isFulfilled() {
        return Filters.countActive(_game, _filter)>=_count;
    }
}

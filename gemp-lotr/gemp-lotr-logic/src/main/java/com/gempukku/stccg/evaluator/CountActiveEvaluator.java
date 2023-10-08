package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CountActiveEvaluator implements Evaluator {
    private final int _over;
    private final Filterable[] _filters;
    private final Integer _limit;

    public CountActiveEvaluator(Filterable... filters) {
        this(null, filters);
    }

    public CountActiveEvaluator(Integer limit, Filterable... filters) {
        this(0, limit, filters);
    }

    public CountActiveEvaluator(int over, Integer limit, Filterable... filters) {
        _over = over;
        _filters = filters;
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        final int active = Math.max(0, Filters.countActive(game, _filters) - _over);
        if (_limit == null)
            return active;
        return Math.min(_limit, active);
    }
}

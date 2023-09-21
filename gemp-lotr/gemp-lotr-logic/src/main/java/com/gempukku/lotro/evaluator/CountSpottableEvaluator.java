package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.evaluator.Evaluator;

import java.util.Objects;

public class CountSpottableEvaluator implements Evaluator {
    private final Evaluator _over;
    private final Filterable[] _filters;
    private final Evaluator _limit;

    public CountSpottableEvaluator(Filterable... filters) {
        this(null, filters);
    }

    public CountSpottableEvaluator(Integer limit, Filterable... filters) {
        this(0, limit, filters);
    }

    public CountSpottableEvaluator(int over, Integer limit, Filterable... filters) {
        _over = new ConstantEvaluator(over);
        _filters = filters;
        _limit = new ConstantEvaluator(Objects.requireNonNullElse(limit, Integer.MAX_VALUE));
    }

    public CountSpottableEvaluator(Evaluator over, Evaluator limit, Filterable... filters) {
        _over = over;
        _filters = filters;
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, LotroPhysicalCard self) {
        final int active = Math.max(0, Filters.countSpottable(game, _filters) - _over.evaluateExpression(game, self));
        if (_limit == null)
            return active;
        return Math.min(_limit.evaluateExpression(game, self), active);
    }
}

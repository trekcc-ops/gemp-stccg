package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardMatchesEvaluator implements Evaluator {
    private final Filterable[] _filters;
    private final Evaluator _matches;
    private final int _default;

    public CardMatchesEvaluator(int defaultValue, Evaluator matches, Filterable... filters) {
        _default = defaultValue;
        _matches = matches;
        _filters = filters;
    }

    public CardMatchesEvaluator(int defaultValue, int matches, Filterable... filters) {
        _default = defaultValue;
        _matches = new ConstantEvaluator(matches);
        _filters = filters;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return Filters.and(_filters).accepts(game, self) ? _matches.evaluateExpression(game, self) : _default;
    }
}

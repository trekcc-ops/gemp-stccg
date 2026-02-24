package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CountCardsMatchingFilterEvaluator extends Evaluator {

    private final CardFilter _filter;
    private final int _forEach;

    public CountCardsMatchingFilterEvaluator(CardFilter filter, int forEach) {
        _filter = filter;
        _forEach = forEach;
    }
    @Override
    public float evaluateExpression(DefaultGame cardGame) {
        return Filters.filter(cardGame, _filter).size() * _forEach;
    }
}
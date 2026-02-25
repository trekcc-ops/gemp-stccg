package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CountCardsMatchingFilterEvaluator extends Evaluator {

    private final CardFilter _filter;
    private final float _forEach;
    private final boolean _roundUp;

    public CountCardsMatchingFilterEvaluator(CardFilter filter, float forEach, boolean roundUp) {
        _filter = filter;
        _forEach = forEach;
        _roundUp = roundUp;
    }
    @Override
    public float evaluateExpression(DefaultGame cardGame) {
        int cardsMatchingFilter = Filters.filter(cardGame, _filter).size();
        float unroundedResult = cardsMatchingFilter * _forEach;
        return (_roundUp) ? (int) Math.ceil(unroundedResult) : (int) unroundedResult;
    }
}
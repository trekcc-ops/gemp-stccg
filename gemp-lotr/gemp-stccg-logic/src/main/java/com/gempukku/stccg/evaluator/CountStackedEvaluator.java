package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CountStackedEvaluator implements Evaluator {
    private final Filterable _stackedOn;
    private final Filterable[] _stackedCard;
    private Integer _limit;

    public CountStackedEvaluator(Filterable stackedOn, Filterable... stackedCard) {
        _stackedOn = stackedOn;
        _stackedCard = stackedCard;
    }

    public CountStackedEvaluator(int limit, Filterable stackedOn, Filterable... stackedCard) {
        this(stackedOn, stackedCard);
        _limit = limit;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        int count = 0;
        for (PhysicalCard card : Filters.filterActive(game, _stackedOn)) {
            count += Filters.filter(game.getGameState().getStackedCards(card), game, _stackedCard).size();
        }
        if (_limit != null)
            return Math.min(_limit, count);
        return count;
    }
}

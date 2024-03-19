package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CountStackedEvaluator extends Evaluator {
    private final Filterable _stackedOn;
    private final Filterable[] _stackedCard;
    private Integer _limit;

    public CountStackedEvaluator(DefaultGame game, Filterable stackedOn, Filterable... stackedCard) {
        super(game);
        _stackedOn = stackedOn;
        _stackedCard = stackedCard;
    }


    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        int count = 0;
        for (PhysicalCard card : Filters.filterActive(_game, _stackedOn)) {
            count += Filters.filter(card.getStackedCards(), _game, _stackedCard).size();
        }
        if (_limit != null)
            return Math.min(_limit, count);
        return count;
    }
}

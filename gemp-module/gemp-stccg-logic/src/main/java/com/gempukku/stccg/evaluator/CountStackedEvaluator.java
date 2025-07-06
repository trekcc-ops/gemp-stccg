package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CountStackedEvaluator extends Evaluator {
    private final Filterable _stackedOn;
    private final Filterable[] _stackedCard;

    public CountStackedEvaluator(Filterable stackedOn, Filterable... stackedCard) {
        super();
        _stackedOn = stackedOn;
        _stackedCard = stackedCard;
    }


    @Override
    public int evaluateExpression(DefaultGame game) {
        int count = 0;
        for (PhysicalCard card : Filters.filterActive(game, _stackedOn)) {
            count += Filters.filter(card.getStackedCards(game), game, _stackedCard).size();
        }
        return count;
    }
}
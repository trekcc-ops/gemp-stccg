package com.gempukku.stccg.condition;

import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardInPlayCondition implements Condition {
    private final Filter _cardFilter;

    public CardInPlayCondition(Filter cardFilter) {
        _cardFilter = cardFilter;
    }


    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        return !Filters.filter(cardGame, Filters.inPlay, _cardFilter).isEmpty();
    }
}
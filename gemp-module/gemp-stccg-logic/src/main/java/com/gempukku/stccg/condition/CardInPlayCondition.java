package com.gempukku.stccg.condition;

import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardInPlayCondition implements Condition {
    private final CardFilter _cardFilter;

    public CardInPlayCondition(CardFilter cardFilter) {
        _cardFilter = cardFilter;
    }


    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        return !Filters.filter(cardGame, Filters.inPlay, _cardFilter).isEmpty();
    }
}
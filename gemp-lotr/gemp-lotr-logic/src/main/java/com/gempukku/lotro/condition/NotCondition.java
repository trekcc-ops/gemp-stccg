package com.gempukku.lotro.condition;

import com.gempukku.lotro.game.DefaultGame;

public class NotCondition implements Condition {
    private final Condition _condition;

    public NotCondition(Condition condition) {
        _condition = condition;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return !_condition.isFulfilled(game);
    }
}

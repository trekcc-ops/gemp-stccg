package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

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

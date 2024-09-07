package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public class NotCondition implements Condition {
    private final Condition _condition;

    public NotCondition(DefaultGame game, Condition condition) {
        _condition = condition;
    }

    @Override
    public boolean isFulfilled() {
        return !_condition.isFulfilled();
    }
}

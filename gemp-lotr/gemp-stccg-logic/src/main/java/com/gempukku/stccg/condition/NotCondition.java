package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public class NotCondition implements Condition {
    private final Condition _condition;
    private final DefaultGame _game;

    public NotCondition(DefaultGame game, Condition condition) {
        _condition = condition;
        _game = game;
    }

    @Override
    public boolean isFulfilled() {
        return !_condition.isFulfilled();
    }
}

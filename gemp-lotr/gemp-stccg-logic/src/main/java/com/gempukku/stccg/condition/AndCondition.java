package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public class AndCondition implements Condition {
    private final Condition[] _conditions;

    public AndCondition(Condition... conditions) {
        _conditions = conditions;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        for (Condition condition : _conditions) {
            if (condition != null && !condition.isFulfilled(game))
                return false;
        }

        return true;
    }
}

package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public class TrueCondition implements Condition {
    @Override
    public boolean isFulfilled(DefaultGame cardGame) { return true; }
}
package com.gempukku.stccg.requirement;

import com.gempukku.stccg.game.DefaultGame;

public class TrueCondition implements Condition {
    @Override
    public boolean isFulfilled(DefaultGame cardGame) { return true; }
}
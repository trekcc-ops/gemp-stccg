package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public interface Condition {
    boolean isFulfilled(DefaultGame cardGame);
}
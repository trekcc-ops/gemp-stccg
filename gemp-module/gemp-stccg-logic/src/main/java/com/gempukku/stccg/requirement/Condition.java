package com.gempukku.stccg.requirement;

import com.gempukku.stccg.game.DefaultGame;

public interface Condition {
    boolean isFulfilled(DefaultGame cardGame);
}
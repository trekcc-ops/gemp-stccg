package com.gempukku.lotro.condition;

import com.gempukku.lotro.game.DefaultGame;

public interface Condition {
    boolean isFulfilled(DefaultGame game);
}

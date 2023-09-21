package com.gempukku.lotro.condition;

import com.gempukku.lotro.game.DefaultGame;

public class MinThreatCondition implements Condition {
    private final int _count;

    public MinThreatCondition(int count) {
        _count = count;
    }

    @Override
    public boolean isFullfilled(DefaultGame game) {
        return game.getGameState().getThreats() >= _count;
    }
}

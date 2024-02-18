package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public abstract class ST1EGameProcess extends GameProcess {
    protected final ST1EGame _game;
    public ST1EGameProcess(ST1EGame game) {
        _game = game;
    }
}
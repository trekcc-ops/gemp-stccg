package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public abstract class ST1EGameProcess extends GameProcess {
    protected final ST1EGame _game;
    public ST1EGameProcess(ST1EGame game) {
        _game = game;
    }
}
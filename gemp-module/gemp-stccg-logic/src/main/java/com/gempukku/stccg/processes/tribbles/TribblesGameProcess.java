package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

public abstract class TribblesGameProcess extends GameProcess {
    protected final TribblesGame _game;

    public TribblesGameProcess(TribblesGame game) {
        _game = game;
    }

}
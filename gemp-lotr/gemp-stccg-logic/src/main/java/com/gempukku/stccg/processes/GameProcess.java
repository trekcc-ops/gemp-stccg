package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;

public interface GameProcess<AbstractGame extends DefaultGame> {
    void process(AbstractGame game);

    GameProcess<AbstractGame> getNextProcess();
}
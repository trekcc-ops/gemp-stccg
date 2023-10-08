package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;

public abstract class DefaultGameProcess<AbstractGame> implements GameProcess {
    public void process(DefaultGame game) {
        AbstractGame abstractGame = (AbstractGame) game;
        process(abstractGame);
    }

    public abstract void process(AbstractGame game);

    public GameProcess getNextProcess() { return null; }
}
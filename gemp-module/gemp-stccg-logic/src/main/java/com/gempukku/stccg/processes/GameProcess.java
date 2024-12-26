package com.gempukku.stccg.processes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

@JsonSerialize(using = GameProcessSerializer.class)
public abstract class GameProcess {
    protected int _consecutivePasses;
    private boolean _isFinished;

    protected GameProcess() { }
    protected GameProcess(int consecutivePasses) {
        _consecutivePasses = consecutivePasses;
    }
    public abstract void process(DefaultGame cardGame);
    public abstract GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException;
    public final void finish() { _isFinished = true; }
    public boolean isFinished() { return _isFinished; }
    public int getConsecutivePasses() { return _consecutivePasses; }
}
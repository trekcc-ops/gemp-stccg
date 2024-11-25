package com.gempukku.stccg.processes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = GameProcessSerializer.class)
public abstract class GameProcess {
    protected int _consecutivePasses;
    private boolean _isFinished;

    protected GameProcess() { }
    protected GameProcess(int consecutivePasses) {
        _consecutivePasses = consecutivePasses;
    }
    public abstract void process();
    public abstract GameProcess getNextProcess();
    public final void finish() { _isFinished = true; }
    public boolean isFinished() { return _isFinished; }
    public int getConsecutivePasses() { return _consecutivePasses; }
}
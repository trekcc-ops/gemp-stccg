package com.gempukku.stccg.processes;

public abstract class GameProcess {
    public abstract void process();
    public abstract GameProcess getNextProcess();
}
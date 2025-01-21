package com.gempukku.stccg.processes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.processes.st1e.SimultaneousGameProcess;

public abstract class GameProcess {
    @JsonProperty("consecutivePasses")
    protected int _consecutivePasses;
    @JsonProperty("isFinished")
    private boolean _isFinished;
    @JsonProperty("className")
    private final String _className = getClass().getSimpleName();

    protected GameProcess() { }
    protected GameProcess(int consecutivePasses) {
        _consecutivePasses = consecutivePasses;
    }
    public abstract void process(DefaultGame cardGame);

    @JsonIgnore
    public abstract GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException;
    public final void finish() { _isFinished = true; }
    public boolean isFinished() { return _isFinished; }
}
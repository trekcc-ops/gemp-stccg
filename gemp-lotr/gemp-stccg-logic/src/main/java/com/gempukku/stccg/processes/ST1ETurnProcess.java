package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1ETurnProcess extends ST1EGameProcess {
    private ST1EGameProcess _followingGameProcess;
    public ST1ETurnProcess(ST1EGame game) {
        super(game);
    }
    @Override
    public void process() {
        _followingGameProcess = new ST1ENormalCardPlayProcess(_game.getGameState().getCurrentPlayerId(), _game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}

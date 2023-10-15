package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1ETurnProcess extends DefaultGameProcess<ST1EGame> {
    private GameProcess _followingGameProcess;
    @Override
    public void process(ST1EGame game) {
        _followingGameProcess = new ST1ENormalCardPlayProcess(game.getGameState().getCurrentPlayerId(),
                new TribblesEndOfTurnGameProcess()
        );
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}

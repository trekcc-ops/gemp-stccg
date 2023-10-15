package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1ENormalCardPlayProcess extends DefaultGameProcess<ST1EGame> {
    private GameProcess _followingGameProcess;
    private String _playerId;

    ST1ENormalCardPlayProcess(String playerId, GameProcess nextProcess) {
        _playerId = playerId;
        _followingGameProcess = nextProcess;
    }
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

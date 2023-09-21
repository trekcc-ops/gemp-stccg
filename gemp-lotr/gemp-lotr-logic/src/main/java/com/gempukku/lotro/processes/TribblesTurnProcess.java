package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.TribblesGame;

public class TribblesTurnProcess extends DefaultGameProcess<TribblesGame> {
    private GameProcess _followingGameProcess;
    @Override
    public void process(TribblesGame game) {
//        game.getGameState().sendMessage("DEBUG: Beginning TribblesTurnProcess");
        _followingGameProcess = new TribblesPlayerPlaysOrDraws(game.getGameState().getCurrentPlayerId(),
                new TribblesEndOfTurnGameProcess()
        );
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}

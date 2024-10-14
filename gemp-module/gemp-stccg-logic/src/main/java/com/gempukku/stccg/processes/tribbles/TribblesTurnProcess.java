package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

public class TribblesTurnProcess extends GameProcess {
    private GameProcess _followingGameProcess;
    private final TribblesGame _game;
    public TribblesTurnProcess(TribblesGame game) {
        _game = game;
    }
    @Override
    public void process() {
//        game.sendMessage("DEBUG: Beginning TribblesTurnProcess");
        _followingGameProcess = new TribblesPlayerPlaysOrDraws(_game.getGameState().getCurrentPlayerId(),
                new TribblesEndOfTurnGameProcess(_game), _game
        );
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
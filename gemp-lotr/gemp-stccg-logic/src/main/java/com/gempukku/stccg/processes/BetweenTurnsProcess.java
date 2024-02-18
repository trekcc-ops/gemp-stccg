package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;

public class BetweenTurnsProcess extends GameProcess {

    private DefaultGame _game;
    private GameProcess _nextProcess;
    public BetweenTurnsProcess(DefaultGame game, GameProcess nextProcess) {
        _game = game;
        _nextProcess = nextProcess;
    }
    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        _game.getGameState().sendMessage("DEBUG: Beginning BetweenTurnsProcess");
        PlayOrder playOrder = _game.getGameState().getPlayerOrder().getClockwisePlayOrder(_game.getGameState().getCurrentPlayerId(), false);
        playOrder.getNextPlayer();

        String nextPlayer = playOrder.getNextPlayer();
        _game.getGameState().startPlayerTurn(nextPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}

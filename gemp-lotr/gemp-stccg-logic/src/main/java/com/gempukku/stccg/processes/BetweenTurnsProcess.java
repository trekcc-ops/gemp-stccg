package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;

public class BetweenTurnsProcess implements GameProcess {

    @Override
    public void process(DefaultGame game) {
        game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        game.getGameState().sendMessage("DEBUG: Beginning BetweenTurnsProcess");
        PlayOrder playOrder = game.getGameState().getPlayerOrder().getClockwisePlayOrder(game.getGameState().getCurrentPlayerId(), false);
        playOrder.getNextPlayer();

        String nextPlayer = playOrder.getNextPlayer();
        game.getGameState().startPlayerTurn(nextPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new StartOfTurnGameProcess();
    }
}

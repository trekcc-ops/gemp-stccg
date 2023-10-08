package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class TribblesBetweenTurnsProcess extends DefaultGameProcess<TribblesGame> {

    @Override
    public void process(TribblesGame game) {
        TribblesGameState gameState = game.getGameState();
        game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        PlayOrder playOrder = gameState.getPlayerOrder().getStandardPlayOrder(gameState.getCurrentPlayerId(), false);

        playOrder.getNextPlayer(); // TODO: This call is necessary but not logical
        String currentPlayer = playOrder.getNextPlayer();

        while (game.getGameState().getPlayerDecked(currentPlayer)) {
            game.getGameState().sendMessage(currentPlayer + " is decked. Skipping their turn.");
            playOrder = gameState.getPlayerOrder().getStandardPlayOrder(currentPlayer, false);
            currentPlayer = playOrder.getNextPlayer();
        }

        game.getGameState().startPlayerTurn(currentPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesStartOfTurnGameProcess();
    }
}

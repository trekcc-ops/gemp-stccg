package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

public class TribblesBetweenTurnsProcess extends TribblesGameProcess {
    public TribblesBetweenTurnsProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        GameState gameState = cardGame.getGameState();
        cardGame.setCurrentPhase(Phase.BETWEEN_TURNS);
        ActionOrder actionOrder = gameState.getPlayerOrder().getStandardPlayOrder(gameState.getCurrentPlayerId(), false);

        actionOrder.getNextPlayer(); // TODO: This call is necessary but not logical
        String currentPlayerId = actionOrder.getNextPlayer();

        while (cardGame.getPlayer(currentPlayerId).isDecked()) {
            actionOrder = gameState.getPlayerOrder().getStandardPlayOrder(currentPlayerId, false);
            currentPlayerId = actionOrder.getNextPlayer();
        }

        Player currentPlayer = cardGame.getPlayer(currentPlayerId);
        gameState.startPlayerTurn(currentPlayer);
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new StartOfTurnGameProcess();
    }
}
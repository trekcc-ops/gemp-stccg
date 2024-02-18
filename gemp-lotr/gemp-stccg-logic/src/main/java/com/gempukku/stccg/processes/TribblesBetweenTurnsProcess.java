package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class TribblesBetweenTurnsProcess extends GameProcess {

    private final TribblesGame _game;
    public TribblesBetweenTurnsProcess(TribblesGame game) {
        super();
        _game = game;
    }
    @Override
    public void process() {
        TribblesGameState gameState = _game.getGameState();
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        PlayOrder playOrder = gameState.getPlayerOrder().getStandardPlayOrder(gameState.getCurrentPlayerId(), false);

        playOrder.getNextPlayer(); // TODO: This call is necessary but not logical
        String currentPlayer = playOrder.getNextPlayer();

        while (_game.getGameState().getPlayerDecked(currentPlayer)) {
            _game.getGameState().sendMessage(currentPlayer + " is decked. Skipping their turn.");
            playOrder = gameState.getPlayerOrder().getStandardPlayOrder(currentPlayer, false);
            currentPlayer = playOrder.getNextPlayer();
        }

        _game.getGameState().startPlayerTurn(currentPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesStartOfTurnGameProcess(_game);
    }
}

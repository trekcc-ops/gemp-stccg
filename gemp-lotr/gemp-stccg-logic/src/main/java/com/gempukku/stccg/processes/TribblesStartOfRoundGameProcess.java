package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class TribblesStartOfRoundGameProcess extends DefaultGameProcess<TribblesGame> {

    private final String _firstPlayer;

    public TribblesStartOfRoundGameProcess(String firstPlayer) {
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(TribblesGame game) {
        TribblesGameState gameState = game.getGameState();
        gameState.advanceRound();

        // Draw new hands. Shuffle only on first round, since shuffling is already done at end of every round.
        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            if (gameState.getRoundNum() == 1) {
                gameState.shuffleDeck(player);
            }
            for (int i = 0; i < game.getFormat().getHandSize(); i++)
                gameState.playerDrawsCard(player);
        }

        gameState.setCurrentPlayerId(_firstPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesStartOfTurnGameProcess();
    }
}

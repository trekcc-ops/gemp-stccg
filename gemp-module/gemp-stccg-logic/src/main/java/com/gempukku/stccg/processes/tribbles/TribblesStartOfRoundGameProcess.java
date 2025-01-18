package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

public class TribblesStartOfRoundGameProcess extends TribblesGameProcess {

    public TribblesStartOfRoundGameProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) {
        TribblesGameState gameState = _game.getGameState();
        gameState.advanceRound();

        // Draw new hands. Shuffle only on first round, since shuffling is already done at end of every round.
        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            if (gameState.getRoundNum() == 1) {
                gameState.shuffleDeck(player);
            }
            for (int i = 0; i < _game.getFormat().getHandSize(); i++)
                gameState.playerDrawsCard(player);
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new StartOfTurnGameProcess();
    }
}
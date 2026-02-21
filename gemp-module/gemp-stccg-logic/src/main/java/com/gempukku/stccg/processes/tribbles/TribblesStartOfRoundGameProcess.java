package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.draw.DrawSingleCardAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

public class TribblesStartOfRoundGameProcess extends TribblesGameProcess {

    public TribblesStartOfRoundGameProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        TribblesGameState gameState = _game.getGameState();
        gameState.advanceRound();

        // Draw new hands. Shuffle only on first round, since shuffling is already done at end of every round.
        for (Player player : cardGame.getPlayers()) {
            if (gameState.getRoundNum() == 1) {
                player.shuffleDrawDeck(cardGame);
            }
            for (int i = 0; i < _game.getFormat().getHandSize(); i++) {
                DrawSingleCardAction drawAction = new DrawSingleCardAction(cardGame, player.getPlayerId());
                drawAction.processEffect(cardGame);
                cardGame.getActionsEnvironment().logCompletedActionNotInStack(drawAction);
                cardGame.sendActionResultToClient();
            }
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new StartOfTurnGameProcess();
    }
}
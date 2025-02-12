package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.TribblesGame;

public class EndTurnAction extends SystemQueueAction {

    public EndTurnAction(DefaultGame cardGame) {
        super(cardGame);
        appendEffect(new AllowResponsesAction(cardGame, ActionResult.Type.END_OF_TURN));
    }
    @Override
    protected void processEffect(DefaultGame cardGame) {
        cardGame.getModifiersEnvironment().signalEndOfTurn();
        cardGame.getActionsEnvironment().signalEndOfTurn();

        if (cardGame instanceof TribblesGame tribblesGame) {
            boolean playerWentOut = false;
            for (Player player : cardGame.getPlayers()) {
                if (player.getCardsInHand().isEmpty()) {
                    playerWentOut = true;
                }
            }
            if (playerWentOut)
                tribblesGame.getGameState().endRound();
        }
        setAsSuccessful();
    }
}
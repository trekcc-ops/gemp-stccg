package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;

public class EndTurnAction extends SystemQueueAction {

    public EndTurnAction(DefaultGame cardGame) {
        super(cardGame);
        appendAction(new AllowResponsesAction(cardGame, EffectResult.Type.END_OF_TURN));
    }
    @Override
    public Action nextAction(DefaultGame cardGame) {
        Action nextAction = getNextAction();
        if (nextAction != null)
            return nextAction;

        cardGame.getModifiersEnvironment().signalEndOfTurn();
        cardGame.getActionsEnvironment().signalEndOfTurn();

        if (cardGame instanceof TribblesGame tribblesGame) {
            boolean playerWentOut = false;
            for (String playerId : cardGame.getPlayerIds()) {
                if (cardGame.getGameState().getHand(playerId).isEmpty()) {
                    playerWentOut = true;
                }
            }
            if (playerWentOut)
                tribblesGame.getGameState().endRound();
        }
        return getNextAction();
    }
}
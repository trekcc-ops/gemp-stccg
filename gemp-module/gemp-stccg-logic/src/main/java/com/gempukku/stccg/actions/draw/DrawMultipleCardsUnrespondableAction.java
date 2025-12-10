package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawMultipleCardsUnrespondableAction extends ActionyAction {

    private final int _cardsToDraw;

    public DrawMultipleCardsUnrespondableAction(DefaultGame cardGame, Player performingPlayer, int cardsToDraw) {
        super(cardGame, performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _cardsToDraw = cardsToDraw;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return !performingPlayer.getCardsInDrawDeck().isEmpty();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (isBeingInitiated())
            setAsInitiated();
        Action nextAction = getNextAction();
        if (nextAction == null) {
            processEffect(cardGame);
        }
        return nextAction;
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            for (int i = 0; i < _cardsToDraw; i++) {
                cardGame.getGameState().playerDrawsCard(cardGame.getPlayer(_performingPlayerId));
            }
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}
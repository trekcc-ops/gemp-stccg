package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawSingleCardAction extends ActionyAction {

    public DrawSingleCardAction(DefaultGame cardGame, Player performingPlayer) {
        super(cardGame, performingPlayer, "Draw a card", ActionType.DRAW_CARD);
    }


    public final boolean requirementsAreMet(DefaultGame cardGame) {
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

    public void processEffect(DefaultGame cardGame) throws PlayerNotFoundException {
        cardGame.getGameState().playerDrawsCard(cardGame.getPlayer(_performingPlayerId));
        setAsSuccessful();
        saveResult(new ActionResult(ActionResult.Type.DRAW_CARD, _performingPlayerId, this));
    }

}
package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawSingleCardAction extends ActionyAction {

    public DrawSingleCardAction(DefaultGame cardGame, String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
    }


    public DrawSingleCardAction(DefaultGame cardGame, Player performingPlayer) {
        this(cardGame, performingPlayer.getPlayerId());
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

    public void processEffect(DefaultGame cardGame) {
        try {
            cardGame.getGameState().playerDrawsCard(cardGame.getPlayer(_performingPlayerId));
            setAsSuccessful();
            saveResult(new ActionResult(ActionResult.Type.DRAW_CARD, _performingPlayerId, this), cardGame);
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}
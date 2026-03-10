package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DrawSingleCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;

    public DrawSingleCardAction(DefaultGame cardGame, String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
        _performingCard = null;
    }

    public DrawSingleCardAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard performingCard) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
        _performingCard = performingCard;
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
            PhysicalCard card = cardGame.getGameState().playerDrawsCard(cardGame.getPlayer(_performingPlayerId));
            if (card != null) {
                setAsSuccessful();
                saveResult(new DrawCardsResult(cardGame, this, List.of(card), false, _performingCard), cardGame);
            } else {
                setAsFailed();
            }
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}
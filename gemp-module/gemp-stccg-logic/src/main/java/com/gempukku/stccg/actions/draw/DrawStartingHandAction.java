package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;

public class DrawStartingHandAction extends ActionyAction {

    private final int _cardsToDraw;

    public DrawStartingHandAction(DefaultGame cardGame, Player performingPlayer, int cardsToDraw) {
        super(cardGame, performingPlayer.getPlayerId(), ActionType.DRAW_CARD);
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

    public void processEffect(DefaultGame cardGame) {
        try {
            Collection<PhysicalCard> cardsDrawn = new ArrayList<>();
            for (int i = 0; i < _cardsToDraw; i++) {
                PhysicalCard cardDrawn = cardGame.getGameState().playerDrawsCard(cardGame.getPlayer(_performingPlayerId));
                cardsDrawn.add(cardDrawn);
            }
            saveResult(new DrawCardsResult(cardGame, this, cardsDrawn, true), cardGame);
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}
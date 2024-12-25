package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateEvolveTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateEvolveTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        // Count the number of cards in your hand
        int cardsInHand = cardGame.getGameState().getHand(_performingPlayerId).size();

        // Place your hand in your discard pile
        appendAction(new DiscardCardAction(_performingCard, performingPlayer,
                cardGame.getGameState().getHand(_performingPlayerId)));

        // Draw that many cards
        appendAction(new DrawCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), cardsInHand));
    }

}
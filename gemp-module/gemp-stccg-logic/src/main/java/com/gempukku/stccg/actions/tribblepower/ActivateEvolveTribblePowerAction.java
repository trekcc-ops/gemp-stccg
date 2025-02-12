package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateEvolveTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateEvolveTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws PlayerNotFoundException {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        // Count the number of cards in your hand
        int cardsInHand = performingPlayer.getCardsInHand().size();

        // Place your hand in your discard pile
        appendEffect(new TribblesMultiDiscardActionBroken(_performingCard, performingPlayer, performingPlayer.getCardsInHand()));

        // Draw that many cards
        appendEffect(new DrawCardsAction(_performingCard, performingPlayer, cardsInHand, cardGame));
    }

}
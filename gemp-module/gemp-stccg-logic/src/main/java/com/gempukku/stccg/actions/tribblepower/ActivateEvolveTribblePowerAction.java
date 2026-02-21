package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateEvolveTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateEvolveTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, ActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        // Count the number of cards in your hand
        int cardsInHand = performingPlayer.getCardsInHand().size();

        // Place your hand in your discard pile
        appendEffect(new TribblesMultiDiscardActionBroken(
                cardGame, _performingCard, _performingPlayerId, performingPlayer.getCardsInHand()));

        // Draw that many cards
        appendEffect(new DrawCardsAction(cardGame, _performingCard, _performingPlayerId, cardsInHand));
    }

}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawSingleCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateCycleTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateCycleTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, GameTextContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(cardGame, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                1, 1);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, performingPlayer, selectAction));
        appendEffect(new DrawSingleCardAction(cardGame, _performingPlayerId));
    }

}
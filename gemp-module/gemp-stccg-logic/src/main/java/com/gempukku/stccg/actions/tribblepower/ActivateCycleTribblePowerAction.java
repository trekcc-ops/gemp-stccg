package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateCycleTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateCycleTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws PlayerNotFoundException {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(cardGame, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                1, 1);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, performingPlayer, selectAction));
        appendEffect(new DrawCardsAction(_performingCard, performingPlayer));
    }

}
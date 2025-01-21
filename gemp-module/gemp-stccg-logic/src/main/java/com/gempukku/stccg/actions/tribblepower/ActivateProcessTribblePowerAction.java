package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateProcessTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateProcessTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame game = actionContext.getGame();
        Player performingPlayer = game.getPlayer(_performingPlayerId);
        appendEffect(new DrawCardsAction(_performingCard, performingPlayer, 3, game));
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                2, 2);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(performingPlayer, selectAction));
    }

}
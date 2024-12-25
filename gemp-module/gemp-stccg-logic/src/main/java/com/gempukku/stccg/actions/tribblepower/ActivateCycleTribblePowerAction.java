package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsOnTableAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateCycleTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateCycleTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectCardsOnTableAction selectAction = new SelectCardsOnTableAction(_performingCard, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                1, 1);
        appendAction(new PlaceCardsOnBottomOfDrawDeckAction(performingPlayer, selectAction, _performingCard));
        appendAction(new DrawCardAction(_performingCard, performingPlayer));
    }

}
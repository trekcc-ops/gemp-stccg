package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateMasakaTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateMasakaTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        for (Player player : cardGame.getPlayers()) {
            int handSize = player.getHand().size();
            SelectCardsAction selectAction = new SelectVisibleCardsAction(actionContext.getSource(),
                    player, "Choose cards in order to put beneath draw deck", Filters.yourHand(player),
                    handSize, handSize);
            appendAction(new PlaceCardsOnBottomOfDrawDeckAction(player, selectAction, actionContext.getSource()));
            appendAction(new DrawCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), 3));
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
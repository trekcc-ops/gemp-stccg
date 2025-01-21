package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
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
            int handSize = player.getCardsInHand().size();
            SelectCardsAction selectAction = new SelectVisibleCardsAction(
                    player, "Choose cards in order to put beneath draw deck", Filters.yourHand(player),
                    handSize, handSize);
            appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(player, selectAction));
            appendEffect(
                    new DrawCardsAction(_performingCard, cardGame.getPlayer(_performingPlayerId), 3, cardGame));
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
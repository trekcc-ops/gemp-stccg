package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateMasakaTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateMasakaTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                            ActionContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        for (Player player : cardGame.getPlayers()) {
            int handSize = player.getCardsInHand().size();
            SelectCardsAction selectAction = new SelectVisibleCardsAction(cardGame,
                    player, "Choose cards in order to put beneath draw deck", Filters.yourHand(player),
                    handSize, handSize);
            appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, player, selectAction));
            appendEffect(
                    new DrawCardsAction(_performingCard, cardGame.getPlayer(_performingPlayerId), 3, cardGame));
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _performingPlayer.getCardsInHand().size() >= 4;
    }

}
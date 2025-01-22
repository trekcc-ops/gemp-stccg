package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardOnBottomOfPlayPileAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateKindnessTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateKindnessTribblePowerAction(TribblesActionContext actionContext, TribblePower power) throws PlayerNotFoundException {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        appendEffect(new DrawCardsAction(_performingCard, cardGame.getPlayer(_performingPlayerId)));
        // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : cardGame.getPlayerIds()) {
            if (cardGame.getGameState().getHand(player).size() >= 4) {
                Player performingPlayer = cardGame.getPlayer(player);
                SelectCardsAction selectAction =
                        new SelectVisibleCardAction(cardGame, performingPlayer,
                                "Select a card to place beneath play pile",
                                Filters.yourHand(performingPlayer));
                appendEffect(new PlaceCardOnBottomOfPlayPileAction(cardGame, performingPlayer, selectAction));
            }
        }
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectionAction = new SelectVisibleCardsAction(cardGame, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                1, 1);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, performingPlayer, selectionAction));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
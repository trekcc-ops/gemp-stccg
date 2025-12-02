package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardOnBottomOfPlayPileAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateKindnessTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateKindnessTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                              ActionContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        appendEffect(new DrawCardsAction(_performingCard, cardGame.getPlayer(_performingPlayerId)));
        // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (Player player : cardGame.getPlayers()) {
            if (player.getCardsInHand().size() >= 4) {
                SelectCardsAction selectAction =
                        new SelectVisibleCardAction(cardGame, player,
                                "Select a card to place beneath play pile",
                                Filters.yourHand(player));
                appendEffect(new PlaceCardOnBottomOfPlayPileAction(cardGame, player, selectAction));
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
        return (_performingPlayer.getCardsInHand().size() >= 4);
    }

}
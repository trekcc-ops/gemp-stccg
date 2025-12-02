package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateProcessTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateProcessTribblePowerAction(TribblesGame game, PhysicalCard performingCard, ActionContext actionContext)
            throws PlayerNotFoundException {
        super(game, actionContext, performingCard);
        Player performingPlayer = game.getPlayer(_performingPlayerId);
        appendEffect(new DrawCardsAction(_performingCard, performingPlayer, 3, game));
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(game, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                2, 2);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(game, performingPlayer, selectAction));
    }

}
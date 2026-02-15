package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateProcessTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateProcessTribblePowerAction(TribblesGame game, PhysicalCard performingCard, ActionContext actionContext) {
        super(game, actionContext, performingCard);
        appendEffect(new DrawCardsAction(_performingCard, _performingPlayerId, 3, game));
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(game, _performingPlayerId,
                "Choose a card to put beneath draw deck", Filters.yourHand(_performingPlayerId),
                2, 2);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(game, _performingPlayerId, selectAction));
    }

}
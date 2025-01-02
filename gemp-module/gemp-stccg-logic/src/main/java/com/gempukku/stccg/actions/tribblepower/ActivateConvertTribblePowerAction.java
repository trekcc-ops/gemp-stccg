package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.TribblesGame;

import java.util.List;


public class ActivateConvertTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateConvertTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(actionContext.getPerformingPlayer(),
                List.of(_performingCard)));
        appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(
                cardGame.getPlayer(_performingPlayerId), 1));
    }

}
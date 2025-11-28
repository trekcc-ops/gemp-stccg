package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;

import java.util.List;


public class ActivateConvertTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateConvertTribblePowerAction(TribblesGame cardGame, TribblesActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, TribblePower.CONVERT);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, actionContext.getPerformingPlayerId(),
                List.of(_performingCard)));
        appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(cardGame,
                cardGame.getPlayer(_performingPlayerId), 1));
    }

}
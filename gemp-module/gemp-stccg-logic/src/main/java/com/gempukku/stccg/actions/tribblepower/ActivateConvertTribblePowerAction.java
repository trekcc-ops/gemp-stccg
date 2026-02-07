package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;


public class ActivateConvertTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateConvertTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, ActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(cardGame, actionContext.getPerformingPlayerId(),
                List.of(_performingCard)));
        appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(cardGame,
                cardGame.getPlayer(_performingPlayerId), 1));
    }

}
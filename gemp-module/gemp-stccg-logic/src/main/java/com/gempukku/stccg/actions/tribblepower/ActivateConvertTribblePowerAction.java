package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.placecard.PlacePlayedCardBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateConvertTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateConvertTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        appendAction(new SubAction(this, new PlacePlayedCardBeneathDrawDeckEffect(_performingCard)));
        appendAction(new SubAction(this,
                new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(cardGame, _performingPlayerId, 1)));
    }

}
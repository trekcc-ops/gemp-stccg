package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collection;
import java.util.LinkedList;


public class ActivateReverseTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateReverseTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_wasCarriedOut) {
            if (cardGame instanceof TribblesGame game) {
                game.getGameState().getPlayerOrder().reversePlayerOrder();
            } else {
                throw new InvalidGameLogicException("Could not use tribble power Mutate in a non-Tribbles game");
            }
            _wasCarriedOut = true;
        }
        return getNextAction();
    }

}
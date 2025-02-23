package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateFamineTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateFamineTribblePowerAction(TribblesActionContext actionContext, TribblePower power) throws PlayerNotFoundException {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (cardGame instanceof TribblesGame game) {
            game.getGameState().setNextTribbleInSequence(1);
        } else {
            throw new InvalidGameLogicException("Could not set tribble sequence in a non-Tribbles game");
        }
        return getNextAction();
    }

}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateReverseTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateReverseTribblePowerAction(TribblesGame cardGame, TribblesActionContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, TribblePower.REVERSE);
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
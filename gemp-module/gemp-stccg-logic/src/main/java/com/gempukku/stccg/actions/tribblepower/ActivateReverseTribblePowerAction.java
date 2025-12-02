package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateReverseTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateReverseTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                             ActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
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
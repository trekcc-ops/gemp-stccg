package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateFamineTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateFamineTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                            ActionContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
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
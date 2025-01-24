package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateDiscardTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateDiscardTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws PlayerNotFoundException {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(cardGame, performingPlayer, "select",
                        Filters.yourHand(performingPlayer));
        appendEffect(new DiscardCardAction(cardGame, _performingCard, performingPlayer, selectAction));
    }

}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateAvalancheTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateAvalancheTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        appendEffect(new AllPlayersDiscardFromHandAction(cardGame, this, false, true));
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(_performingCard, performingPlayer, "select",
                        Filters.yourHand(performingPlayer));
        appendEffect(new DiscardCardAction(_performingCard, performingPlayer, selectAction));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
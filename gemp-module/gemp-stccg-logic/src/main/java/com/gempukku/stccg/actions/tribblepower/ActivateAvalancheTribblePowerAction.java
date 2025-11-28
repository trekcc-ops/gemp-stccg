package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateAvalancheTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateAvalancheTribblePowerAction(TribblesGame cardGame, TribblesActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, TribblePower.AVALANCHE);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        appendEffect(new AllPlayersDiscardFromHandAction(cardGame, this, actionContext.getSource(), false, true));
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(cardGame, performingPlayer, "select",
                        Filters.yourHand(performingPlayer));
        appendEffect(new TribblesMultiDiscardActionBroken(cardGame, _performingCard, performingPlayer, selectAction));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _performingPlayer.getCardsInHand().size() >= 4;
    }

}
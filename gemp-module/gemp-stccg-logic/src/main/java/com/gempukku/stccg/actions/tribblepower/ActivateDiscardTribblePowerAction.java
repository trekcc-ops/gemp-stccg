package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;


public class ActivateDiscardTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateDiscardTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, ActionContext actionContext)
            throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(cardGame, _performingPlayerId, "select",
                        Filters.yourHand(performingPlayer));
        appendEffect(new TribblesMultiDiscardActionBroken(cardGame, _performingCard, performingPlayer, selectAction));
    }

}
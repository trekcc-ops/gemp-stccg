package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateAvalancheTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateAvalancheTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard, ActionContext actionContext) {
        super(cardGame, actionContext, performingCard);
        appendEffect(new AllPlayersDiscardFromHandAction(cardGame, this, performingCard, false, true));
        SelectVisibleCardAction selectAction =
                new SelectVisibleCardAction(cardGame, _performingPlayerId, "select",
                        Filters.yourHand(_performingPlayerId));
        appendEffect(new TribblesMultiDiscardActionBroken(cardGame, _performingCard, _performingPlayerId, selectAction));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return cardGame.getGameState().getCardGroup(_performingPlayerId, Zone.HAND).size() >= 4;
    }

}
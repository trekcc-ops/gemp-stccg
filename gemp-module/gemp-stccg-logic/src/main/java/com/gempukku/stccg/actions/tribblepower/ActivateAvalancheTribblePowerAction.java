package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.AwaitingDecisionType;
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
        appendAction(new SubAction(this,
                new AllPlayersDiscardFromHandEffect(cardGame, this, false, true)));
        SelectCardInPlayAction selectAction =
                new SelectCardInPlayAction(_performingCard, performingPlayer, "select",
                        Filters.yourHand(performingPlayer), AwaitingDecisionType.CARD_SELECTION);
        appendAction(new DiscardCardAction(_performingCard, performingPlayer, selectAction));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
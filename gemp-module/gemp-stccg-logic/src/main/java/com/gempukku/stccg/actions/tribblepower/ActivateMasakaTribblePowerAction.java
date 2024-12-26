package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.placecard.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;


public class ActivateMasakaTribblePowerAction extends ActivateTribblePowerAction {
    public ActivateMasakaTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        for (String player : cardGame.getPlayerIds()) {
            for (PhysicalCard card : cardGame.getGameState().getHand(player)) {
                appendAction(new SubAction(this, new PutCardsFromZoneOnEndOfPileEffect(
                        cardGame, false, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, card)));
            }
            appendAction(new DrawCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), 3));
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;



public class ActivateMasakaTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMasakaTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (getGame().getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        for (String player : getGame().getPlayerIds()) {
            for (PhysicalCard card : getGame().getGameState().getHand(player)) {
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(
                        getGame(), false, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
            }
            subAction.appendEffect(new DrawCardsEffect(getGame(), _action, player, 3));
        }
        return addActionAndReturnResult(getGame(), subAction);
    }
}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;



public class ActivateMasakaTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMasakaTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (_game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        for (String player : _game.getPlayerIds()) {
            for (PhysicalCard card : _game.getGameState().getHand(player)) {
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(
                        _game, false, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
            }
            subAction.appendEffect(new DrawCardsEffect(_game, _action, player, 3));
        }
        return addActionAndReturnResult(_game, subAction);
    }
}
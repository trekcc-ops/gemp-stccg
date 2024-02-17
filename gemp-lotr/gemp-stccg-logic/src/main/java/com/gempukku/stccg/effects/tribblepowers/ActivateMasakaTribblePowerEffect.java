package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.abstractsubaction.DrawCardsEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.effects.defaulteffect.PutCardsFromZoneOnEndOfPileEffect;

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
        SubAction subAction = new SubAction(_action);
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
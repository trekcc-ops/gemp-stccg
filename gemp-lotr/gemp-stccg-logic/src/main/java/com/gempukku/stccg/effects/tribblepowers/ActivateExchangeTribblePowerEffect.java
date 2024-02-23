package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.choose.ChooseCardAndMoveBetweenZonesEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.filters.Filters;

public class ActivateExchangeTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateExchangeTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                _game, _action, _activatingPlayer, false, 1, 1, Filters.any));
        subAction.appendEffect(new ChooseCardAndMoveBetweenZonesEffect(
                _game, Zone.DISCARD, Zone.HAND, _action, _activatingPlayer, 1, 1, Filters.any));
        return addActionAndReturnResult(_game, subAction);
    }
}
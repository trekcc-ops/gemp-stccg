package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.choose.ChooseCardAndMoveBetweenZonesEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;

public class ActivateExchangeTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateExchangeTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                getGame(), _action, _activatingPlayer, false, 1, 1, Filters.any));
        subAction.appendEffect(new ChooseCardAndMoveBetweenZonesEffect(
                getGame(), Zone.DISCARD, Zone.HAND, _action, _activatingPlayer, 1, 1, Filters.any));
        return addActionAndReturnResult(getGame(), subAction);
    }
}
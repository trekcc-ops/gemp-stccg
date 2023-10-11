package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.abstractsubaction.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.filters.Filters;

public class ActivateCycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateCycleTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                _game, _action, _activatingPlayer, 1, false, Filters.any));
        subAction.appendEffect(new DrawCardsEffect(_game, _action, _activatingPlayer, 1));
        return addActionAndReturnResult(_game, subAction);
    }
}
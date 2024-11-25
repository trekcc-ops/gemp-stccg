package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.filters.Filters;

public class ActivateCycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateCycleTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action, _game);
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                getGame(), _action, _activatingPlayer, 1, false, Filters.any));
        subAction.appendEffect(new DrawCardsEffect(getGame(), _action, _activatingPlayer, 1));
        return addActionAndReturnResult(getGame(), subAction);
    }
}
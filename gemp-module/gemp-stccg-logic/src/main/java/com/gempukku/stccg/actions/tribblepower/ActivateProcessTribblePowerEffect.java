package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.filters.Filters;

public class ActivateProcessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateProcessTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action, _game);
        subAction.appendEffect(new DrawCardsEffect(getGame(), subAction, _activatingPlayer, 3));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                getGame(), subAction, _activatingPlayer, 2, false, Filters.any));
        return addActionAndReturnResult(getGame(), subAction);
    }
}
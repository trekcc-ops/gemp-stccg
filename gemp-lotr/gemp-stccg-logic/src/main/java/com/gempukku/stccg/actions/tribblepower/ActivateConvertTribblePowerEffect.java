package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlacePlayedCardBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;




public class ActivateConvertTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateConvertTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new PlacePlayedCardBeneathDrawDeckEffect(_game, _source));
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(_game, _activatingPlayer, 1));
        return addActionAndReturnResult(_game, subAction);
    }
}
package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.PlacePlayedCardBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;




public class ActivateConvertTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateConvertTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action, _game);
        subAction.appendEffect(new PlacePlayedCardBeneathDrawDeckEffect(_source));
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(getGame(), _activatingPlayer, 1));
        return addActionAndReturnResult(getGame(), subAction);
    }
}
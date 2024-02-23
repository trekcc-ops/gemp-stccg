package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.effects.defaulteffect.PlacePlayedCardBeneathDrawDeckEffect;
import com.gempukku.stccg.effects.defaulteffect.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;

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
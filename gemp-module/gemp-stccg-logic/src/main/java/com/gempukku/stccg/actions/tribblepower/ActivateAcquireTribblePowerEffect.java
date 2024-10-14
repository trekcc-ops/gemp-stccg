package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.cards.TribblesActionContext;



public class ActivateAcquireTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAcquireTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        FullEffectResult result;
        if (isPlayableInFull()) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(new AllPlayersDiscardFromHandEffect(_action, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    getGame(), _action, _activatingPlayer,false,1));
            result = addActionAndReturnResult(getGame(), subAction);
        }
        else
            result = new FullEffectResult(false);
        return result;
    }
}
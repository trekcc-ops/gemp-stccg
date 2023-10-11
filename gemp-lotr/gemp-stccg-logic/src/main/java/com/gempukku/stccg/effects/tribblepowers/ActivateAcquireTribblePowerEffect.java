package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.effects.defaulteffect.AllPlayersDiscardFromHandEffect;

public class ActivateAcquireTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAcquireTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {

        if (isPlayableInFull()) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new AllPlayersDiscardFromHandEffect(_game, _action, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    _game, _action, _activatingPlayer,false,1));
            return addActionAndReturnResult(_game, subAction);
        }
        else
            return new FullEffectResult(false);
    }
}
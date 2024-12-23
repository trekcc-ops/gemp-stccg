package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.cards.TribblesActionContext;



public class ActivateAcquireTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAcquireTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        FullEffectResult result;
        if (isPlayableInFull()) {
            SubAction subAction = new SubAction(_action, _game);
            subAction.appendEffect(
                    new AllPlayersDiscardFromHandEffect(_game, _action, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    getGame(), _action, _activatingPlayer,false,1));
            result = addActionAndReturnResult(getGame(), subAction);
        }
        else
            result = new FullEffectResult(false);
        return result;
    }
}
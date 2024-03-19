package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.cards.TribblesActionContext;



public class ActivateAvalancheTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAvalancheTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (_game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(new AllPlayersDiscardFromHandEffect(_game, _action, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    _game, _action, _activatingPlayer,false,1));
            return addActionAndReturnResult(_game, subAction);
        }
        else
            return new FullEffectResult(false);
    }
}
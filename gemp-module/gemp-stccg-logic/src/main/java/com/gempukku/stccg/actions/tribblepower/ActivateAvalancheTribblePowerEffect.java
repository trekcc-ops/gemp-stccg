package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.discard.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.cards.TribblesActionContext;



public class ActivateAvalancheTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAvalancheTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    public boolean isPlayableInFull() {
        return (getGame().getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            SubAction subAction = new SubAction(_action, _game);
            subAction.appendEffect(
                    new AllPlayersDiscardFromHandEffect(_game, _action, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    getGame(), _action, _activatingPlayer,false,1));
            return addActionAndReturnResult(getGame(), subAction);
        }
        else
            return new FullEffectResult(false);
    }
}
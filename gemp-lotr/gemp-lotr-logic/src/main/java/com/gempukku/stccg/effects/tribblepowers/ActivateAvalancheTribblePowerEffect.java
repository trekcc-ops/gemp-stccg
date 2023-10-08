package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AllPlayersDiscardFromHandEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateAvalancheTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAvalancheTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return (game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        if (isPlayableInFull(game)) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new AllPlayersDiscardFromHandEffect(_action, _source, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    _action, _activatingPlayer,false,1));
            return addActionAndReturnResult(game, subAction);
        }
        else
            return new FullEffectResult(false);
    }
}
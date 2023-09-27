package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateAcquireTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateAcquireTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {

        if (isPlayableInFull(game)) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new AllPlayersDiscardEffect(_action, _source, false, true));
            subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                    _action, _activatingPlayer,false,1));
            return addActionAndReturnResult(game, subAction);
        }
        else
            return new FullEffectResult(false);
    }
}
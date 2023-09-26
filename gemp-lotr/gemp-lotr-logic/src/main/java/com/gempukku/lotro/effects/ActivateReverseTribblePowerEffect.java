package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateReverseTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateReverseTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        game.getGameState().getPlayerOrder().reversePlayerOrder();
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}
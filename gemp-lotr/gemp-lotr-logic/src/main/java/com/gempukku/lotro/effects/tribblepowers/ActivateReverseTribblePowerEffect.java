package com.gempukku.lotro.effects.tribblepowers;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateReverseTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateReverseTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        game.getGameState().getPlayerOrder().reversePlayerOrder();
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }
}
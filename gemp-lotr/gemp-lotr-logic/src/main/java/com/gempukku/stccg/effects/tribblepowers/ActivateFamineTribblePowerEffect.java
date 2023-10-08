package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateFamineTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateFamineTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        game.getGameState().setNextTribbleInSequence(1);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }
}
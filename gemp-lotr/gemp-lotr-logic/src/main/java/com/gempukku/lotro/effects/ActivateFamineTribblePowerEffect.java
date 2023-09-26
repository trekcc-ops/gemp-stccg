package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateFamineTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateFamineTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        game.getGameState().setNextTribbleInSequence(1);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}
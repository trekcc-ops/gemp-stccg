package com.gempukku.lotro.effects.tribblepowers;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateDiscardTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateDiscardTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        new ChooseAndDiscardCardsFromHandEffect(_action, _source.getOwner(),false,1).playEffect(game);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }
}
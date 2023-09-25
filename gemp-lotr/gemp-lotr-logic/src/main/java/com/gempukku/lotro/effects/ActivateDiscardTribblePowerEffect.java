package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.game.DefaultGame;

public class ActivateDiscardTribblePowerEffect extends ActivateTribblePowerEffect {
    Action _action;

    public ActivateDiscardTribblePowerEffect(Action action, LotroPhysicalCard source) {
        super(source);
        _action = action;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        new ChooseAndDiscardCardsFromHandEffect(_action, _source.getOwner(),false,1).playEffect(game);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}
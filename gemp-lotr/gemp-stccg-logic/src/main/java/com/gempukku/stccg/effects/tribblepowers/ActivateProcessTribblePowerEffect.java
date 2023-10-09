package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateProcessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateProcessTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new DrawCardsEffect(subAction, _activatingPlayer, 3));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                subAction, _activatingPlayer, 2, false, Filters.any));
        game.getActionsEnvironment().emitEffectResult(_result);
        return new AbstractEffect.FullEffectResult(true);
    }
}
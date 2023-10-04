package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateProcessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateProcessTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new DrawCardsEffect(subAction, _activatingPlayer, 3));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                subAction, _activatingPlayer, 2, false, Filters.any));
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}
package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateCycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateCycleTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                _action, _activatingPlayer, 1, false, Filters.any));
        subAction.appendEffect(new DrawCardsEffect(_action, _activatingPlayer, 1));
        return addActionAndReturnResult(game, subAction);
    }
}
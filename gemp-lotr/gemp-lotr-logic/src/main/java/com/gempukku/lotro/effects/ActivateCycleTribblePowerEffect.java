package com.gempukku.lotro.effects;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class ActivateCycleTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateCycleTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source,
                                           DefaultActionContext actionContext) {
        super(action, source, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        SubAction subAction = new SubAction(_action);
        _action.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                _action, _activatingPlayer, 1, false, Filters.any));
        _action.appendEffect(new DrawCardsEffect(_action, _activatingPlayer, 1));
        return addActionAndReturnResult(game, subAction);
    }
}
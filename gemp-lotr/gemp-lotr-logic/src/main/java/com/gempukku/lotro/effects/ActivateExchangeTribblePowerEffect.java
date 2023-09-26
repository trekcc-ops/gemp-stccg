package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class ActivateExchangeTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateExchangeTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        SubAction subAction = new SubAction(_action);
        _action.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                _action, _activatingPlayer, false, 1, 1, Filters.any));
        _action.appendEffect(new ChooseAndPutCardFromDiscardIntoHandEffect(
                _action, _activatingPlayer, 1, 1, Filters.any));
        return addActionAndReturnResult(game, subAction);
    }
}
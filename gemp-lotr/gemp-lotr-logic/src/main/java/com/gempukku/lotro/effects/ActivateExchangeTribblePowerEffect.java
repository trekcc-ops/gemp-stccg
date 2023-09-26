package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateExchangeTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateExchangeTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
        super(action, source);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                _action, _activatingPlayer, false, 1, 1, Filters.any));
        subAction.appendEffect(new ChooseAndPutCardFromDiscardIntoHandEffect(
                _action, _activatingPlayer, 1, 1, Filters.any));
        return addActionAndReturnResult(game, subAction);
    }
}
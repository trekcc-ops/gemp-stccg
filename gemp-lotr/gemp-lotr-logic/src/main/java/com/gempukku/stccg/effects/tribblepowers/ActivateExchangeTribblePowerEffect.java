package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateExchangeTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateExchangeTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(
                _action, _activatingPlayer, false, 1, 1, Filters.any));
        subAction.appendEffect(new ChooseAndPutCardFromDiscardIntoHandEffect(
                _action, _activatingPlayer, 1, 1, Filters.any));
        return addActionAndReturnResult(game, subAction);
    }
}
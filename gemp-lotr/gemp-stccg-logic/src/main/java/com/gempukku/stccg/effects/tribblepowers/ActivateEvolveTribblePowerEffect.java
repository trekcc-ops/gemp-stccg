package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.abstractsubaction.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;
import com.gempukku.stccg.filters.Filters;

public class ActivateEvolveTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateEvolveTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();

        // Count the number of cards in your hand
        int cardsInHand = _game.getGameState().getHand(_activatingPlayer).size();

        // Place your hand in your discard pile
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(_game, _action, _activatingPlayer, false,
                        cardsInHand, cardsInHand, Filters.any));

        // Draw that many cards
        subAction.appendEffect(new DrawCardsEffect(_game, _action, _activatingPlayer, cardsInHand));

        return addActionAndReturnResult(_game, subAction);
    }
}
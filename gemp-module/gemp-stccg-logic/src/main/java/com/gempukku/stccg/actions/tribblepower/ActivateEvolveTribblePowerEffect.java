package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.filters.Filters;

public class ActivateEvolveTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateEvolveTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = new SubAction(_action, _game);

        // Count the number of cards in your hand
        int cardsInHand = getGame().getGameState().getHand(_activatingPlayer).size();

        // Place your hand in your discard pile
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(getGame(), _action, _activatingPlayer, false,
                        cardsInHand, cardsInHand, Filters.any));

        // Draw that many cards
        subAction.appendEffect(new DrawCardsEffect(getGame(), _action, _activatingPlayer, cardsInHand));

        return addActionAndReturnResult(getGame(), subAction);
    }
}
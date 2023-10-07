package com.gempukku.lotro.effects.tribblepowers;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.effects.DrawCardsEffect;
import com.gempukku.lotro.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateEvolveTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateEvolveTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);

        // Count the number of cards in your hand
        int cardsInHand = game.getGameState().getHand(_activatingPlayer).size();

        // Place your hand in your discard pile
        subAction.appendEffect(new ChooseAndDiscardCardsFromHandEffect(_action, _activatingPlayer, false,
                        cardsInHand, cardsInHand, Filters.any));

        // Draw that many cards
        subAction.appendEffect(new DrawCardsEffect(_action, _activatingPlayer, cardsInHand));

        return addActionAndReturnResult(game, subAction);
    }
}
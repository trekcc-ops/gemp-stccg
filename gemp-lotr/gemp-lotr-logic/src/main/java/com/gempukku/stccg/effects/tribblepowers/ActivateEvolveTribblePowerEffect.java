package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

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
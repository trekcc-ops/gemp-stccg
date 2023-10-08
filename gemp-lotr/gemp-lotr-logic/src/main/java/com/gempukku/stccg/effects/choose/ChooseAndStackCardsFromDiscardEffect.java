package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.effects.StackCardFromDiscardEffect;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;
import java.util.List;

public class ChooseAndStackCardsFromDiscardEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final PhysicalCard _stackOn;
    private final Filterable[] _filter;

    public ChooseAndStackCardsFromDiscardEffect(Action action, String playerId, int minimum, int maximum, PhysicalCard stackOn, Filterable... filter) {
        _action = action;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackOn = stackOn;
        _filter = filter;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filter(game.getGameState().getDiscard(_playerId), game, _filter).size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        Collection<PhysicalCard> discard = Filters.filter(game.getGameState().getDiscard(_playerId), game, _filter);

        final boolean success = discard.size() >= _minimum;

        if (discard.size() <= _minimum) {
            SubAction subAction = new SubAction(_action);
            for (PhysicalCard card : discard)
                subAction.appendEffect(new StackCardFromDiscardEffect(card, _stackOn));
            game.getActionsEnvironment().addActionToStack(subAction);
            stackFromDiscardCallback(discard);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose cards to stack", discard, _minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> cards = getSelectedCardsByResponse(result);
                            SubAction subAction = new SubAction(_action);
                            for (PhysicalCard card : cards)
                                subAction.appendEffect(new StackCardFromDiscardEffect(card, _stackOn));
                            game.getActionsEnvironment().addActionToStack(subAction);
                            stackFromDiscardCallback(cards);
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    public void stackFromDiscardCallback(Collection<PhysicalCard> cardsStacked) {

    }
}
package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ChooseStackedCardsEffect extends AbstractEffect {
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filterable _stackedOnFilter;
    private final Filterable _stackedCardFilter;

    public ChooseStackedCardsEffect(String playerId, int minimum, int maximum, Filterable stackedOnFilter, Filterable stackedCardFilter) {
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackedOnFilter = stackedOnFilter;
        _stackedCardFilter = stackedCardFilter;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Choose stacked card";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.countActive(game, _stackedOnFilter, Filters.hasStacked(_stackedCardFilter)) > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        List<PhysicalCard> stackedCards = new LinkedList<>();

        for (PhysicalCard stackedOnCard : Filters.filterActive(game, _stackedOnFilter))
            stackedCards.addAll(Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, _stackedCardFilter));

        int maximum = Math.min(_maximum, stackedCards.size());

        final boolean success = stackedCards.size() >= _minimum;

        if (stackedCards.size() <= _minimum) {
            cardsChosen(game, stackedCards);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, getText(game), stackedCards, _minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> stackedCards = getSelectedCardsByResponse(result);
                            cardsChosen(game, stackedCards);
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsChosen(DefaultGame game, Collection<PhysicalCard> stackedCards);
}

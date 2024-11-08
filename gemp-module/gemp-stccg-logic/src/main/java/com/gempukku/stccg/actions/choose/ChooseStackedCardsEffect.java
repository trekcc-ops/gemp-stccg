package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ChooseStackedCardsEffect extends DefaultEffect {
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filterable _stackedOnFilter;
    private final Filterable _stackedCardFilter;

    public ChooseStackedCardsEffect(DefaultGame game, String playerId, int minimum, int maximum, Filterable stackedOnFilter, Filterable stackedCardFilter) {
        super(game, playerId);
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackedOnFilter = stackedOnFilter;
        _stackedCardFilter = stackedCardFilter;
    }

    @Override
    public String getText() {
        return "Choose stacked card";
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.countActive(_game, _stackedOnFilter, Filters.hasStacked(_stackedCardFilter)) > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        List<PhysicalCard> stackedCards = new LinkedList<>();

        for (PhysicalCard stackedOnCard : Filters.filterActive(_game, _stackedOnFilter))
            stackedCards.addAll(Filters.filter(stackedOnCard.getStackedCards(), _game, _stackedCardFilter));

        int maximum = Math.min(_maximum, stackedCards.size());

        final boolean success = stackedCards.size() >= _minimum;

        if (stackedCards.size() <= _minimum) {
            cardsChosen(stackedCards);
        } else {
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(_game.getPlayer(_playerId), getText(), stackedCards, _minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> stackedCards = getSelectedCardsByResponse(result);
                            cardsChosen(stackedCards);
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsChosen(Collection<PhysicalCard> stackedCards);
}
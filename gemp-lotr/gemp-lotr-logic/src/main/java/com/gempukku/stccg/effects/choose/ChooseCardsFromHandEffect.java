package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.AbstractEffect;

import java.util.Collection;
import java.util.Set;

public abstract class ChooseCardsFromHandEffect extends AbstractEffect {
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromHandEffect(String playerId, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Choose cards from hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filter).size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        final Collection<PhysicalCard> selectableCards = Filters.filter(game.getGameState().getHand(_playerId), game, _filter);
        int maximum = Math.min(_maximum, selectableCards.size());

        boolean success = selectableCards.size() >= _minimum;

        int minimum = Math.min(_minimum, selectableCards.size());

        if (minimum == selectableCards.size())
            cardsSelected(game, selectableCards);
        else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, getText(game), selectableCards, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            cardsSelected(game, selectedCards);
                        }
                    }
            );
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards);
}

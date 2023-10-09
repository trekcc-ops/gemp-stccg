package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.AbstractEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public abstract class ChooseCardsFromDiscardEffect extends AbstractEffect {
    private final String _playerId;
    private final String _targetPlayerDiscardId;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromDiscardEffect(String playerId, int minimum, int maximum, Filterable... filters) {
        this(playerId, playerId, minimum, maximum, filters);
    }

    public ChooseCardsFromDiscardEffect(String playerId, String targetPlayerDiscardId, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _targetPlayerDiscardId = targetPlayerDiscardId;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Choose card from discard";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getDiscard(_targetPlayerDiscardId), game, _filter);
        return cards.size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getDiscard(_targetPlayerDiscardId), game, _filter);

        boolean success = cards.size() >= _minimum;

        int minimum = Math.min(_minimum, cards.size());

        if (_maximum == 0) {
            cardsSelected(game, Collections.emptySet());
        } else if (cards.size() == minimum) {
            cardsSelected(game, cards);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose card from discard", new LinkedList<>(cards), minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            cardsSelected(game, getSelectedCardsByResponse(result));
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards);
}

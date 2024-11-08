package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public abstract class ChooseArbitraryCardsEffect extends DefaultEffect {
    private final String _playerId;
    private final String _choiceText;
    private final boolean _showMatchingOnly;
    private final Collection<PhysicalCard> _cards;
    private final Filterable _filter;
    private final int _minimum;
    private final int _maximum;

    public ChooseArbitraryCardsEffect(DefaultGame game, String playerId, String choiceText, Collection<? extends PhysicalCard> cards, int minimum, int maximum) {
        this(game, playerId, choiceText, cards, Filters.any, minimum, maximum, false);
    }

    public ChooseArbitraryCardsEffect(DefaultGame game, String playerId, String choiceText, Collection<? extends PhysicalCard> cards, Filterable filter, int minimum, int maximum, boolean showMatchingOnly) {
        super(game, playerId);
        _playerId = playerId;
        _choiceText = choiceText;
        _showMatchingOnly = showMatchingOnly;
        _cards = new HashSet<>(cards);
        _filter = filter;
        _minimum = minimum;
        _maximum = maximum;
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.filter(_cards, _game, _filter).size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        Collection<PhysicalCard> possibleCards = Filters.filter(_cards, _game, _filter);

        boolean success = possibleCards.size() >= _minimum;

        int minimum = _minimum;

        if (possibleCards.size() < minimum)
            minimum = possibleCards.size();

        if (_maximum == 0) {
            cardsSelected(Collections.emptySet());
        } else if (possibleCards.size() == minimum) {
            cardsSelected(possibleCards);
        } else {
            Collection<PhysicalCard> toShow = _cards;
            if (_showMatchingOnly)
                toShow = possibleCards;

            _game.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(_game.getPlayer(_playerId), _choiceText, toShow, possibleCards,
                            _minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            cardsSelected(getSelectedCardsByResponse(result));
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(Collection<PhysicalCard> selectedCards);
}
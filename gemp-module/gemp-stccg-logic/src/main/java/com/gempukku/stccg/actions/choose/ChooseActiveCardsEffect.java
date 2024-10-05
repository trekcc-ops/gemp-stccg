package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public abstract class ChooseActiveCardsEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private String _choiceText;
    private final int _minimum;
    private final int _maximum;
    private final Filterable[] _filters;
    private boolean _shortcut = true;

    public ChooseActiveCardsEffect(PhysicalCard source, String playerId, String choiceText, int minimum, int maximum,
                                   Filterable... filters) {
        super(source.getGame(), playerId);
        _source = source;
        _playerId = playerId;
        _choiceText = choiceText;
        _minimum = minimum;
        _maximum = maximum;
        _filters = filters;
    }

    public ChooseActiveCardsEffect(ActionContext actionContext, String playerId, String choiceText, int minimum,
                                   int maximum, Filterable... filters) {
        super(actionContext.getGame(), playerId);
        _source = actionContext.getSource();
        _playerId = playerId;
        _choiceText = choiceText;
        _minimum = minimum;
        _maximum = maximum;
        _filters = filters;
    }

    protected Filter getExtraFilterForPlaying() {
        return Filters.any;
    }

    protected Filter getExtraFilterForPlayabilityCheck() {
        return getExtraFilterForPlaying();
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.countActive(_game, Filters.and(
                _filters, getExtraFilterForPlayabilityCheck())) >= _minimum;
    }

    @Override
    public String getText() {
        return _choiceText;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        final Collection<PhysicalCard> matchingCards = Filters.filterActive(_game, Filters.and(_filters,
                getExtraFilterForPlaying()));
        // Let's get the count realistic
        int maximum = Math.min(_maximum, matchingCards.size());

        int minimum = _minimum;
        if (matchingCards.size() < minimum)
            minimum = matchingCards.size();

        if (_shortcut && maximum == 0) {
            cardsSelected(Collections.emptySet());
        } else if (_shortcut && matchingCards.size() == minimum) {
            if (_source != null && !matchingCards.isEmpty())
                _game.getGameState().cardAffectsCard(_playerId, _source, matchingCards);
            cardsSelected(matchingCards);
        } else {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, _choiceText, matchingCards, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (_source != null && !selectedCards.isEmpty())
                                _game.getGameState().cardAffectsCard(_playerId, _source, selectedCards);
                            cardsSelected(selectedCards);
                        }
                    });
        }

        return new FullEffectResult(matchingCards.size() >= _minimum);
    }

    protected abstract void cardsSelected(Collection<PhysicalCard> cards);

}

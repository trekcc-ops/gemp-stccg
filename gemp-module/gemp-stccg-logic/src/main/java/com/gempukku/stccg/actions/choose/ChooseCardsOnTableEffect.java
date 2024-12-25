package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * An effect that causes the specified player to choose cards on the table.
 * <p>
 * Note: The choosing of cards provided by this effect does not involve persisting the cards selected or any targeting
 * reasons. This is just choosing cards, and calling the cardsSelected method with the card chosen.
 */
public abstract class ChooseCardsOnTableEffect extends DefaultEffect {
    private String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Collection<? extends PhysicalCard> _cards;
    private final Filterable _filters;
    private final String _choiceText;
    private final Action _action;

    public ChooseCardsOnTableEffect(Action action, Player player, String choiceText,
                                    Collection<? extends PhysicalCard> cards) {
        this(action, player, choiceText, 1,1,cards,Filters.any);
    }

    public ChooseCardsOnTableEffect(Action action, Player player, String choiceText, int minimum, int maximum,
                                    Collection<? extends PhysicalCard> cards) {
        this(action, player, choiceText, minimum, maximum, cards, Filters.any);
    }

    public ChooseCardsOnTableEffect(Action action, Player player, String choiceText, int minimum, int maximum,
                                    Collection<? extends PhysicalCard> cards, Filterable filters) {
        super(player);
        _action = action;
        _playerId = player.getPlayerId();
        _choiceText = choiceText;
        _minimum = minimum;
        _maximum = maximum;
        _cards = cards;
        _filters = Filters.and(Filters.onTable, filters);
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.countActive(_game, _filters) >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // If player is not set, set to current player to make choices
        if (_playerId == null) {
            _playerId = _game.getGameState().getCurrentPlayerId();
        }

        // Determine the cards to choose from
        Collection<PhysicalCard> selectableCards;
        if (_cards != null)
            selectableCards = Filters.filter(_cards, _game, _filters);
        else
            selectableCards = Filters.filterActive(_game, _action.getPerformingCard(), _filters);

        // Make sure at least the minimum number of cards can be found
        if (selectableCards.size() < _minimum) {
            return new FullEffectResult(false);
        }

        // Adjust the min and max card counts
        int maximum = Math.min(_maximum, selectableCards.size());
        final int minimum = _minimum;

        if (maximum == 0) {
            cardsSelected(Collections.emptySet());
        }
        else if (selectableCards.size() == minimum) {
            cardsSelected(selectableCards);
        }
        else {
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(_game.getPlayer(_playerId), _choiceText, selectableCards, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            cardsSelected(selectedCards);
                        }
                    });
        }

        return new FullEffectResult(true);
    }

    /**
     * This method is called when cards have been selected.
     * @param selectedCards the selected cards
     */
    protected abstract void cardsSelected(Collection<PhysicalCard> selectedCards);
}
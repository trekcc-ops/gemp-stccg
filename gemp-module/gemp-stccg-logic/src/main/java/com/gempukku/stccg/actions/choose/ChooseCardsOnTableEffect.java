package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private boolean cardSelectionFailed;
    private final Action _action;
    private final DefaultGame _game;

    public ChooseCardsOnTableEffect(Action action, String playerId, String choiceText, Collection<? extends PhysicalCard> cards) {
        this(action, playerId, choiceText, 1,1,cards,Filters.any);
    }

    public ChooseCardsOnTableEffect(Action action, String playerId, String choiceText,
                                    Stream<? extends PhysicalCard> cards) {
        this(action, playerId, choiceText, 1,1,cards.collect(Collectors.toSet()),Filters.any);
    }

    public ChooseCardsOnTableEffect(Action action, Player player, String choiceText,
                                    int minimum, int maximum, Collection<PhysicalCard> cards) {
        this(action, player.getPlayerId(), choiceText, minimum, maximum, cards, Filters.any);
    }

    public ChooseCardsOnTableEffect(Action action, String playerId, String choiceText,
                                    int minimum, int maximum, Collection<PhysicalCard> cards) {
        this(action, playerId, choiceText, minimum, maximum, cards, Filters.any);
    }

    /**
     * Creates an effect that causes the player to choose cards from the specified collection of cards on the table accepted
     * by the specified filter.
     *
     * @param minimum               the minimum number of cards to choose
     * @param maximum               the maximum number of cards to choose
     * @param maximumAcceptsCount   the maximum number of times cards may be accepted by the filter, which will further limit
     *                              cards that can be selected when cards with multiple model types accept filter multiple times
     * @param matchPartialModelType true if card with multiple model types (i.e. squadrons) match if any model type
     *                              matches the filter otherwise card only matches if all model types match the filter
     * @param filters               the filter
     * @param action                the action performing this effect
     * @param playerId              the player
     * @param choiceText            the text shown to the player choosing the cards
     * @param cards                 the cards to choose from
     */
    public ChooseCardsOnTableEffect(Action action, String playerId, String choiceText, int minimum, int maximum,
                                    Collection<? extends PhysicalCard> cards, Filterable filters) {
        super(playerId);
        _action = action;
        _playerId = playerId;
        _choiceText = choiceText;
        _minimum = minimum;
        _maximum = maximum;
        _cards = cards;
        _filters = Filters.and(Filters.onTable, filters);
        _game = action.getGame();
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.countActive(_game, _filters) >= _minimum;
    }

    private boolean isAllowAbort() { return false; }

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
            selectableCards = Filters.filterActive(_game, _action.getActionSource(), _filters);

        // Filter cards by accounting for cards with multiple classes
/*        int acceptsCountSoFar = 0;
        List<PhysicalCard> validCards = new LinkedList<>();
        for (PhysicalCard selectableCard : selectableCards) {
            int acceptsCount = Filters.and(_filters).acceptsCount(_game, selectableCard);
            if (acceptsCount > 0 && acceptsCount <= _maximumAcceptsCount) {
                validCards.add(selectableCard);
                acceptsCountSoFar += acceptsCount;
            }
        }
        selectableCards = validCards;*/

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
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1,_choiceText + ((minimum > 0 && isAllowAbort()) ? ", or click 'Done' to cancel" : ""), selectableCards, isAllowAbort() ? 0 : minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            cardsSelected(selectedCards);
                        }
                    });
        }

        return new FullEffectResult(true);
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && !cardSelectionFailed;
    }

    /**
     * This method is called when cards have been selected.
     * @param selectedCards the selected cards
     */
    protected abstract void cardsSelected(Collection<PhysicalCard> selectedCards);
}

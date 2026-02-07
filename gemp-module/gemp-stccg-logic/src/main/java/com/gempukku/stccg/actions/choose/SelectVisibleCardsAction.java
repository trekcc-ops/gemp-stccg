package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An effect that causes the specified player to choose cards on the table.
 */
public class SelectVisibleCardsAction extends ActionyAction implements SelectCardsAction {
    private final static int UNLIMITED_MAXIMUM = 999999;
    private Collection<PhysicalCard> _selectedCards = new LinkedList<>();
    private final CardFilter _selectableCardFilter;
    private final int _minimum;
    private Integer _maximum;
    private final String _decisionText;

    public SelectVisibleCardsAction(DefaultGame cardGame, String performingPlayerName, String choiceText,
                                    CardFilter selectionFilter, int minimum, int maximum) {
        super(cardGame, performingPlayerName, ActionType.SELECT_CARDS);
        _selectableCardFilter = selectionFilter;
        _minimum = minimum;
        _maximum = maximum;
        _decisionText = choiceText;
    }

    public SelectVisibleCardsAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                    Collection<? extends PhysicalCard> cards, int minimum) {
        this(cardGame, selectingPlayerName, choiceText, Filters.inCards(cards), minimum, UNLIMITED_MAXIMUM);
    }

    public SelectVisibleCardsAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                    CardFilter selectionFilter, int minimum, int maximum) {
        this(cardGame, selectingPlayer.getPlayerId(), choiceText, selectionFilter, minimum, maximum);
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return getSelectableCards(game).size() >= _minimum;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<? extends PhysicalCard> selectableCards = getSelectableCards(cardGame);

        _maximum = Math.min(_maximum, selectableCards.size());

        if (selectableCards.size() == _minimum) {
            _selectedCards.addAll(selectableCards);
        } else {
            cardGame.sendAwaitingDecision(
                    new CardsSelectionDecision(_performingPlayerId, _decisionText, selectableCards,
                            _minimum, _maximum, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _selectedCards = getSelectedCardsByResponse(result);
                            setAsSuccessful();
                        }
                    });
        }
        setAsSuccessful();
    }

    public Collection<PhysicalCard> getSelectedCards() { return _selectedCards; }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return Filters.filter(cardGame, _selectableCardFilter);
    }

    public int getMinimum() { return _minimum; }
    public int getMaximum() { return _maximum; }

}
package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;

public class SelectCardsFromDialogAction extends ActionyAction implements SelectCardsAction {
    private final int _minimum;
    private final int _maximum;
    private Collection<PhysicalCard> _selectedCards;
    private AwaitingDecision _decision;
    private final CardFilter _selectableCardFilter;
    private final String _decisionText;


    public SelectCardsFromDialogAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                       CardFilter cardFilter, int count) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _decisionText = choiceText;
        _selectableCardFilter = cardFilter;
        _minimum = count;
        _maximum = count;
    }

    public SelectCardsFromDialogAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter,
                                       int count) {
        this(cardGame, selectingPlayerName, "Select cards", cardFilter, count);
    }


    public SelectCardsFromDialogAction(DefaultGame cardGame, Player selectingPlayer, String choiceText,
                                       CardFilter cardFilter) {
        this(cardGame, selectingPlayer.getPlayerId(), choiceText, cardFilter, 1);
    }

    public SelectCardsFromDialogAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                       CardFilter cardFilter) {
        this(cardGame, selectingPlayerName, choiceText, cardFilter, 1);
    }

    public boolean requirementsAreMet(DefaultGame game) {
        try {
            return getSelectableCards(game).size() >= _minimum;
        } catch(Exception exp) {
            return true;
        }
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<? extends PhysicalCard> selectableCards = getSelectableCards(cardGame);
        if (selectableCards.size() == _minimum) {
            _selectedCards = new LinkedList<>(selectableCards);
            setAsSuccessful();
            setCardToMemory();
        } else if (_decision == null) {
            _decision = new ArbitraryCardsSelectionDecision(_performingPlayerId, _decisionText, selectableCards,
                                _minimum, _maximum, cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCards = getSelectedCardsByResponse(result);
                                setAsSuccessful();
                                setCardToMemory();
                            }
                        };
            cardGame.sendAwaitingDecision(_decision);
        }
    }

    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return _selectedCards;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return Filters.filter(cardGame, _selectableCardFilter);
    }

    private void setCardToMemory() {
    }

    public int getMinimum() { return _minimum; }
    public int getMaximum() { return _maximum; }
}
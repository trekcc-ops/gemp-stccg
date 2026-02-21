package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An effect that causes the specified player to choose a card on the table.
 */
public class SelectVisibleCardAction extends ActionyAction implements SelectCardAction {
    private PhysicalCard _selectedCard;
    private final String _decisionText;
    private final Collection<PhysicalCard> _selectableCards;

    public SelectVisibleCardAction(DefaultGame cardGame, String selectingPlayerName, String choiceText,
                                   Collection<? extends PhysicalCard> cards) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _selectableCards = new ArrayList<>(cards);
        _decisionText = choiceText;
    }

    public SelectVisibleCardAction(DefaultGame cardGame, String playerName, String choiceText, CardFilter cardFilter) {
        this(cardGame, playerName, choiceText, Filters.filter(cardGame, cardFilter));
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !getSelectableCards(game).isEmpty();
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<? extends PhysicalCard> selectableCards = getSelectableCards(cardGame);
        if (selectableCards.size() == 1) {
            _selectedCard = Iterables.getOnlyElement(selectableCards);
        } else {
            AwaitingDecision decision = new CardsSelectionDecision(
                    _performingPlayerId, _decisionText, selectableCards,
                                1, 1, cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                _selectedCard = getSelectedCardByResponse(result);
                                setAsSuccessful();
                            }
                        };
            cardGame.sendAwaitingDecision(decision);
        }
        setAsSuccessful();
    }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return _selectableCards;
    }
}
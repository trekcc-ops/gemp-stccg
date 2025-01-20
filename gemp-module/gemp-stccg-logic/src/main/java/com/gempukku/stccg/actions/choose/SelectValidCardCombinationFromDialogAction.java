package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SelectValidCardCombinationFromDialogAction extends ActionyAction implements SelectCardsAction {
    private final Collection<? extends PhysicalCard> _selectableCards;
    private final static int MINIMUM = 0;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private final int _maximum;
    private final String _choiceText;
    private Collection<PhysicalCard> _selectedCards;

    public SelectValidCardCombinationFromDialogAction(Player performingPlayer, String choiceText,
                                                      Collection<PhysicalCard> selectableCards,
                                                      Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                                      int maximum) {
        super(performingPlayer, choiceText, ActionType.SELECT_CARD);
        _selectableCards = selectableCards;
        _maximum = maximum;
        _validCombinations = validCombinations;
        _choiceText = choiceText;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !_selectableCards.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        AwaitingDecision decision = new ArbitraryCardsSelectionDecision(performingPlayer, _choiceText,
                _selectableCards, _validCombinations, MINIMUM, _maximum) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                _selectedCards = getSelectedCardsByResponse(result);
                _wasCarriedOut = true;
            }
        };

        cardGame.getUserFeedback().sendAwaitingDecision(decision);

        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return _selectedCards;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) { return _selectableCards; }

    public int getMinimum() { return MINIMUM; }
    public int getMaximum() { return _maximum; }

}
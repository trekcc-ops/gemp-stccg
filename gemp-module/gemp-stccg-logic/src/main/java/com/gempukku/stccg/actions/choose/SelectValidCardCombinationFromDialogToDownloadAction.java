package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SelectValidCardCombinationFromDialogToDownloadAction extends ActionyAction implements SelectCardsAction {
    private final Collection<? extends PhysicalCard> _selectableCards;
    private final static int MINIMUM = 1;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private final int _maximum;
    private final String _choiceText;
    private Collection<PhysicalCard> _selectedCards;

    public SelectValidCardCombinationFromDialogToDownloadAction(DefaultGame cardGame, Player performingPlayer, String choiceText,
                                                                Collection<PhysicalCard> selectableCards,
                                                                Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                                                int maximum) {
        super(cardGame, performingPlayer, choiceText, ActionType.SELECT_CARDS);
        _selectableCards = selectableCards;
        _maximum = maximum;
        _validCombinations = validCombinations;
        if (selectableCards.isEmpty()) {
            _choiceText = "No cards can be downloaded";
        } else {
            _choiceText = "Select " + MINIMUM + " to " + maximum + " cards";
        }
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !_selectableCards.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        AwaitingDecision decision = new ArbitraryCardsSelectionDecision(performingPlayer, _choiceText,
                _selectableCards, _validCombinations, MINIMUM, _maximum, cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                _selectedCards = getSelectedCardsByResponse(result);
                setAsSuccessful();
            }
        };

        cardGame.sendAwaitingDecision(decision);

        setAsSuccessful();
        return getNextAction();
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
package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithRespondableInitiation;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectRandomCardAction extends ActionyAction implements ActionWithRespondableInitiation, SelectCardAction {

    private PhysicalCard _selectedCard;
    private final AllCardsMatchingFilterResolver _targetResolver;
    private final List<PhysicalCard> _requiredCards = new ArrayList<>();

    public SelectRandomCardAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _targetResolver = new AllCardsMatchingFilterResolver(cardFilter);
        _cardTargets.add(_targetResolver);
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !_targetResolver.cannotBeResolved(game);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            if (!_requiredCards.isEmpty()) {
                _selectedCard = Iterables.getOnlyElement(_requiredCards);
                setAsSuccessful();
            } else {
                Collection<? extends PhysicalCard> selectableCards = getSelectableCards(cardGame);
                if (selectableCards.isEmpty()) {
                    setAsFailed();
                    throw new InvalidGameLogicException("Could not select a random card from an empty list");
                } else {
                    _selectedCard = TextUtils.getRandomItemFromList(selectableCards);
                    if (_selectedCard == null) {
                        setAsFailed();
                    } else {
                        setAsSuccessful();
                    }
                }
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public PhysicalCard getSelectedCard() {
        return _selectedCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return _targetResolver.getCards();
    }

    @Override
    public void saveInitiationResult(DefaultGame cardGame) {
        saveResult(new RandomSelectionInitiatedResult(this, getSelectableCards(cardGame)), cardGame);
    }

    public void setCardToRequired(PhysicalCard volunteeringCard) {
        _requiredCards.add(volunteeringCard);
    }
}
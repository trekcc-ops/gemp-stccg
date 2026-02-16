package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithRespondableInitiation;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectRandomCardsAction extends ActionyAction implements ActionWithRespondableInitiation, SelectCardsAction {

    private final Collection<PhysicalCard> _selectedCards = new ArrayList<>();
    private final AllCardsMatchingFilterResolver _targetResolver;
    private final List<PhysicalCard> _requiredCards = new ArrayList<>();
    private String _saveToMemoryId;
    private final int _count;

    public SelectRandomCardsAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter,
                                   ActionContext context, String saveToMemoryId, int count) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS, context);
        _targetResolver = new AllCardsMatchingFilterResolver(cardFilter);
        _cardTargets.add(_targetResolver);
        _saveToMemoryId = saveToMemoryId;
        _count = count;
    }

    public SelectRandomCardsAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter, int count) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _targetResolver = new AllCardsMatchingFilterResolver(cardFilter);
        _cardTargets.add(_targetResolver);
        _count = count;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !_targetResolver.cannotBeResolved(game);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            if (_requiredCards.size() > _count) {
                throw new InvalidGameLogicException("Volunteered too many cards for this selection");
            } else {
                Collection<? extends PhysicalCard> selectableCards = new ArrayList<>(getSelectableCards(cardGame));
                if (!_requiredCards.isEmpty()) {
                    for (PhysicalCard card : _requiredCards) {
                        _selectedCards.add(card);
                        selectableCards.remove(card);
                    }
                }
                int cardsToSelect = _count - _selectedCards.size();
                if (selectableCards.size() < cardsToSelect) {
                    setAsFailed();
                    throw new InvalidGameLogicException("Not enough cards to select");
                } else {
                    List<PhysicalCard> additionalCards = TextUtils.getRandomItemsFromList(selectableCards, cardsToSelect);
                    _selectedCards.addAll(additionalCards);
                    if (_selectedCards.size() != _count) {
                        setAsFailed();
                    } else {
                        saveToContext();
                        setAsSuccessful();
                    }
                }
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }
    @Override
    public Collection<PhysicalCard> getSelectedCards() {
        return _selectedCards;
    }

    @Override
    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return _targetResolver.getCards();
    }

    @Override
    public int getMinimum() {
        return _count;
    }

    @Override
    public int getMaximum() {
        return _count;
    }

    @Override
    public void saveInitiationResult(DefaultGame cardGame) {
        saveResult(new RandomSelectionInitiatedResult(this, getSelectableCards(cardGame)), cardGame);
    }

    public void setCardToRequired(PhysicalCard volunteeringCard) {
        _requiredCards.add(volunteeringCard);
    }

    private void saveToContext() {
        if (_actionContext != null && _saveToMemoryId != null) {
            _actionContext.setCardMemory(_saveToMemoryId, _selectedCards);
        }
    }
}
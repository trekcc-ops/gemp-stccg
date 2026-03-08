package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithRespondableInitiation;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
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
    private final int _min;
    private final int _max;

    public SelectRandomCardsAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter,
                                   GameTextContext context, String saveToMemoryId, int min, int max) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS, context);
        _targetResolver = new AllCardsMatchingFilterResolver(cardFilter);
        _cardTargets.add(_targetResolver);
        _saveToMemoryId = saveToMemoryId;
        _min = min;
        _max = max;
    }

    public SelectRandomCardsAction(DefaultGame cardGame, String selectingPlayerName, CardFilter cardFilter, int count) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_CARDS);
        _targetResolver = new AllCardsMatchingFilterResolver(cardFilter);
        _cardTargets.add(_targetResolver);
        _min = count;
        _max = count;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        return !_targetResolver.cannotBeResolved(game);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            if (_max < _min) {
                setAsFailed();
            } else if (_requiredCards.size() > _min) {
                throw new InvalidGameLogicException("Volunteered too many cards for this selection");
            } else {
                Collection<? extends PhysicalCard> selectableCards = new ArrayList<>(getSelectableCards(cardGame));
                if (!_requiredCards.isEmpty()) {
                    for (PhysicalCard card : _requiredCards) {
                        _selectedCards.add(card);
                        selectableCards.remove(card);
                    }
                }
                if (_min == _max) {
                    int cardsToSelect = _max - _selectedCards.size();
                    if (selectableCards.size() < cardsToSelect) {
                        setAsFailed();
                        throw new InvalidGameLogicException("Not enough cards to select");
                    } else {
                        Collection<PhysicalCard> additionalCards =
                                cardGame.getRandomSelectionOfCards(selectableCards, cardsToSelect);
                        _selectedCards.addAll(additionalCards);
                        if (_selectedCards.size() != _min) {
                            setAsFailed();
                        } else {
                            saveToContext();
                            setAsSuccessful();
                        }
                    }
                } else {
                    AwaitingDecision decision = new IntegerAwaitingDecision(_performingPlayerId, DecisionContext.SELECT_NUMBER,
                            _min - _selectedCards.size(), _max - _selectedCards.size(), cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            int numberResult = getValidatedResult(result);
                            if (selectableCards.size() < numberResult) {
                                setAsFailed();
                                throw new DecisionResultInvalidException("Not enough cards to select");
                            } else {
                                Collection<PhysicalCard> additionalCards =
                                        cardGame.getRandomSelectionOfCards(selectableCards, numberResult);
                                _selectedCards.addAll(additionalCards);
                                if (_selectedCards.size() < _min || _selectedCards.size() > _max) {
                                    setAsFailed();
                                } else {
                                    saveToContext();
                                    setAsSuccessful();
                                }
                            }
                        }
                    };
                    cardGame.sendAwaitingDecision(decision);
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
        return _min;
    }

    @Override
    public int getMaximum() {
        return _max;
    }

    @Override
    public void saveInitiationResult(DefaultGame cardGame) {
        saveResult(new RandomSelectionInitiatedResult(cardGame, this, getSelectableCards(cardGame)), cardGame);
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
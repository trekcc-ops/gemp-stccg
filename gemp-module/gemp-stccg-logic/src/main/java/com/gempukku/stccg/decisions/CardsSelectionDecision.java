package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.CardSelectionDecisionResponse;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class CardsSelectionDecision extends AbstractAwaitingDecision implements CardSelectionDecision {
    private final List<? extends PhysicalCard> _selectableCards;

    @JsonProperty("min")
    private final int _minimum;

    @JsonProperty("max")
    private final int _maximum;

    private final String[] _cardIds;

    protected final List<PhysicalCard> _decisionSelectedCards = new ArrayList<>();

    @JsonProperty("independentlySelectable")
    private final boolean _independentlySelectable = true;

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  DefaultGame cardGame) {
        this(player, text, physicalCards, 0, physicalCards.size(), cardGame);
    }

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  int minimum, int maximum, DefaultGame cardGame) {
        super(player, text, cardGame);
        _selectableCards = new LinkedList<PhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        _cardIds = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            _cardIds[i] = String.valueOf(_selectableCards.get(i).getCardId());
    }

    public String[] getCardIds() {
        return _cardIds;
    }

    public String getElementType() { return "CARD"; }

    protected PhysicalCard getSelectedCardByResponse(String response) throws DecisionResultInvalidException {
        if (_minimum != 1 || _maximum != 1 || response.isEmpty())
            throw new DecisionResultInvalidException();
        try {
            String cardId = response;
            PhysicalCard result = getSelectedCardById(Integer.parseInt(cardId));
            return result;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException("Invalid decision response");
        }
    }

    public void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException {
        if (_responded || !_decisionSelectedCards.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        } else if (response instanceof CardSelectionDecisionResponse cardResponse) {
            List<Integer> cardIds = cardResponse.getCardIds();
            if (cardIds == null || cardIds.size() < _minimum || cardIds.size() > _maximum) {
                throw new DecisionResultInvalidException("Received incorrect number of cards in response to decision");
            }
            List<PhysicalCard> cardsToAdd = new ArrayList<>();
            for (Integer cardId : cardIds) {
                try {
                    PhysicalCard cardToAdd = cardGame.getCardFromCardId(cardId);
                    if (cardToAdd != null && _selectableCards.contains(cardToAdd)) {
                        cardsToAdd.add(cardToAdd);
                    } else {
                        throw new DecisionResultInvalidException("Selected invalid card for decision");
                    }
                } catch(CardNotFoundException exp) {
                    throw new DecisionResultInvalidException(exp.getMessage());
                }
            }
            _decisionSelectedCards.addAll(cardsToAdd);
            _responded = true;
        }
    }

    public void setDecisionResponse(List<PhysicalCard> cards) throws DecisionResultInvalidException {
        if (_responded || !_decisionSelectedCards.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        }
        if (cards == null || cards.size() < _minimum || cards.size() > _maximum) {
            throw new DecisionResultInvalidException("Received incorrect number of cards in response to decision");
        }
        List<PhysicalCard> cardsToAdd = new ArrayList<>();
        for (PhysicalCard card : cards) {
            if (card != null && _selectableCards.contains(card)) {
                cardsToAdd.add(card);
            } else {
                throw new DecisionResultInvalidException("Selected invalid card for decision");
            }
        }
        _decisionSelectedCards.addAll(cardsToAdd);
        _responded = true;
    }

    private PhysicalCard getSelectedCardById(int cardId) throws DecisionResultInvalidException {
        for (PhysicalCard physicalCard : _selectableCards)
            if (physicalCard.getCardId() == cardId)
                return physicalCard;

        throw new DecisionResultInvalidException();
    }

    public void setResponseAndFollowUp(PhysicalCard card) throws DecisionResultInvalidException, InvalidGameOperationException {
        setResponseAndFollowUp(List.of(card));
    }

    public void setResponseAndFollowUp(List<PhysicalCard> cards) throws DecisionResultInvalidException, InvalidGameOperationException {
        try {
            setDecisionResponse(cards);
            followUp();
        } catch(InvalidGameLogicException exp) {
            throw new InvalidGameOperationException(exp.getMessage());
        }
    }

    @JsonProperty("displayedCards")
    private List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (PhysicalCard card : _selectableCards) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", String.valueOf(card.getCardId()));
            mapToAdd.put("selectable", true);
            result.add(mapToAdd);
        }
        return result;
    }

    public List<? extends PhysicalCard> getSelectableCards() {
        return _selectableCards;
    }

}
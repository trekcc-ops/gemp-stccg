package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.CardSelectionDecisionResponse;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class ArbitraryCardsSelectionDecision extends AbstractAwaitingDecision implements CardSelectionDecision {
    private final List<PhysicalCard> _physicalCards = new LinkedList<>();
    private final Collection<? extends PhysicalCard> _selectable;

    @JsonProperty("min")
    private final int _minimum;

    @JsonProperty("max")
    private final int _maximum;
    
    private Map<PhysicalCard, List<Integer>> _validCombinations;

    @JsonProperty("cardIds")
    private final String[] _cardIds;

    @JsonProperty("independentlySelectable")
    private final boolean _independentlySelectable;

    protected final List<PhysicalCard> _decisionSelectedCards = new ArrayList<>();

    public ArbitraryCardsSelectionDecision(Player player, String text,
                                           Collection<? extends PhysicalCard> physicalCards, DefaultGame cardGame) {
        this(player, text, physicalCards, physicalCards, 0, physicalCards.size(), cardGame);
    }



    public ArbitraryCardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                           int minimum, int maximum, DefaultGame cardGame) {
        this(player, text, physicalCards, physicalCards, minimum, maximum, cardGame);
    }


    public ArbitraryCardsSelectionDecision(Player player, String text,
                                           Collection<? extends PhysicalCard> physicalCards,
                                           Collection<? extends PhysicalCard> selectable, int minimum, int maximum,
                                           DefaultGame cardGame) {
        super(player, text, cardGame);
        _physicalCards.addAll(physicalCards);
        _selectable = selectable;
        _minimum = minimum;
        _maximum = maximum;
        _cardIds = getCardIds(physicalCards);
        _independentlySelectable = true;
    }

    public ArbitraryCardsSelectionDecision(Player player, String text,
                                           Collection<? extends PhysicalCard> physicalCards,
                                           Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                           int minimum, int maximum, DefaultGame cardGame) {
        super(player, text, cardGame);
        _physicalCards.addAll(physicalCards);
        _selectable = physicalCards;
        _minimum = minimum;
        _maximum = maximum;
        _validCombinations = new HashMap<>();

        for (PersonnelCard personnel : validCombinations.keySet()) {
            List<Integer> pairingsList = new LinkedList<>();
            for (PersonnelCard pairing : validCombinations.get(personnel)) {
                pairingsList.add(pairing.getCardId());
            }
            _validCombinations.put(personnel, pairingsList);
        }

        _cardIds = getCardIds(physicalCards);
        _independentlySelectable = false;
    }

    public String getElementType() { return "CARD"; }


    private String[] getCardIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = String.valueOf(physicalCard.getCardId());
            index++;
        }
        return result;
    }

    // Only used for testing
    public String getCardIdForCard(PhysicalCard card) throws InvalidGameLogicException {
        return String.valueOf(card.getCardId());
    }

    public void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException {
        if (_responded || !_decisionSelectedCards.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        } else if (response instanceof CardSelectionDecisionResponse cardResponse) {
            if (cardResponse.getCardIds() == null) {
                throw new DecisionResultInvalidException("Unable to identify cards selected for decision");
            }
            List<PhysicalCard> responseCards = new ArrayList<>();
            for (Integer cardId : cardResponse.getCardIds()) {
                try {
                    PhysicalCard cardToAdd = cardGame.getCardFromCardId(cardId);
                    if (cardToAdd != null) {
                        responseCards.add(cardToAdd);
                    } else {
                        throw new DecisionResultInvalidException("Selected invalid card for decision");
                    }
                } catch (CardNotFoundException exp) {
                    throw new DecisionResultInvalidException(exp.getMessage());
                }
            }
            setDecisionResponse(responseCards);
        }
    }

    public void setDecisionResponse(List<PhysicalCard> cards) throws DecisionResultInvalidException {
        if (_responded || !_decisionSelectedCards.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        }
        if (cards.size() < _minimum || cards.size() > _maximum) {
            throw new DecisionResultInvalidException("Received incorrect number of cards in response to decision");
        }
        List<PhysicalCard> cardsToAdd = new ArrayList<>();
        for (PhysicalCard cardToAdd : cards) {
            // TODO - Check against valid combinations
            if (cardToAdd != null && _selectable.contains(cardToAdd)) {
                cardsToAdd.add(cardToAdd);
            } else {
                throw new DecisionResultInvalidException("Selected invalid card for decision");
            }
        }
        _decisionSelectedCards.addAll(cardsToAdd);
        _responded = true;
    }

    protected List<PhysicalCard> getSelectedCardsByResponse(String response) throws DecisionResultInvalidException {
        String[] cardIds;
        if (response.isEmpty())
            cardIds = new String[0];
        else
            cardIds = response.split(",");

        if (cardIds.length < _minimum || cardIds.length > _maximum)
            throw new DecisionResultInvalidException();

        List<PhysicalCard> result = new LinkedList<>();
        try {
            for (String cardId : cardIds) {
                List<String> cardIdList = Arrays.asList(_cardIds);
                int idNum = cardIdList.indexOf(cardId);
                PhysicalCard card = _physicalCards.get(idNum);
                if (result.contains(card) || !_selectable.contains(card))
                    throw new DecisionResultInvalidException();
                result.add(card);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new DecisionResultInvalidException();
        }

        return result;
    }

    public void setResponseAndFollowUp(PhysicalCard card) throws DecisionResultInvalidException, InvalidGameOperationException {
        setResponseAndFollowUp(List.of(card));
    }


    public void setResponseAndFollowUp(List<PhysicalCard> cards) throws DecisionResultInvalidException, InvalidGameOperationException {
        setDecisionResponse(cards);
        try {
            followUp();
        } catch(InvalidGameLogicException exp) {
            throw new InvalidGameOperationException(exp.getMessage());
        }
    }

    @JsonProperty("displayedCards")
    private List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (PhysicalCard card : _physicalCards) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", card.getCardId());
            mapToAdd.put("selectable", _selectable.contains(card));
            if (_validCombinations != null && _validCombinations.get(card) != null) {
                mapToAdd.put("compatibleCardIds", _validCombinations.get(card));
            }
            result.add(mapToAdd);
        }
        return result;
    }

    public String[] getCardIds() {
        return _cardIds;
    }

    public List<PhysicalCard> getSelectedCards() {
        return _decisionSelectedCards;
    }

    public List<? extends PhysicalCard> getSelectableCards() {
        return new ArrayList<PhysicalCard>(_selectable);
    }

}
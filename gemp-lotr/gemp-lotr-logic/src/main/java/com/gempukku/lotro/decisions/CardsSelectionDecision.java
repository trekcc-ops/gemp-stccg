package com.gempukku.lotro.decisions;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;

import java.util.*;

public abstract class CardsSelectionDecision extends AbstractAwaitingDecision {
    private final List<? extends LotroPhysicalCard> _physicalCards;
    private final int _minimum;
    private final int _maximum;

    public CardsSelectionDecision(int id, String text, Collection<? extends LotroPhysicalCard> physicalCard) {
        this(id, text, physicalCard, 0, physicalCard.size());
    }

    public CardsSelectionDecision(int id, String text, Collection<? extends LotroPhysicalCard> physicalCards, int minimum, int maximum) {
        super(id, text, AwaitingDecisionType.CARD_SELECTION);
        _physicalCards = new LinkedList<LotroPhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getCardIds(_physicalCards));
    }

    private String[] getCardIds(List<? extends LotroPhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            result[i] = String.valueOf(physicalCards.get(i).getCardId());
        return result;
    }

    protected Set<LotroPhysicalCard> getSelectedCardsByResponse(String response) throws DecisionResultInvalidException {
        if (response.equals("")) {
            if (_minimum == 0)
                return Collections.emptySet();
            else
                throw new DecisionResultInvalidException();
        }
        String[] cardIds = response.split(",");
        if (cardIds.length < _minimum || cardIds.length > _maximum)
            throw new DecisionResultInvalidException();

        Set<LotroPhysicalCard> result = new HashSet<>();
        try {
            for (String cardId : cardIds) {
                LotroPhysicalCard card = getSelectedCardById(Integer.parseInt(cardId));
                if (result.contains(card))
                    throw new DecisionResultInvalidException();
                result.add(card);
            }
        } catch (NumberFormatException e) {
            throw new DecisionResultInvalidException();
        }

        return result;
    }

    private LotroPhysicalCard getSelectedCardById(int cardId) throws DecisionResultInvalidException {
        for (LotroPhysicalCard physicalCard : _physicalCards)
            if (physicalCard.getCardId() == cardId)
                return physicalCard;

        throw new DecisionResultInvalidException();
    }
}
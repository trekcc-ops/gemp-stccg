package com.gempukku.stccg.decisions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class CardsSelectionDecision extends AbstractAwaitingDecision {
    private final List<? extends PhysicalCard> _physicalCards;
    private final int _minimum;
    private final int _maximum;

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  DefaultGame cardGame) {
        this(player, text, physicalCards, 0, physicalCards.size(), cardGame);
    }

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  int minimum, int maximum, DefaultGame cardGame) {
        super(player, text, AwaitingDecisionType.CARD_SELECTION, cardGame);
        _physicalCards = new LinkedList<PhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getCardIds(_physicalCards));
    }

    private String[] getCardIds(List<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            result[i] = String.valueOf(physicalCards.get(i).getCardId());
        return result;
    }

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

    protected Set<PhysicalCard> getSelectedCardsByResponse(String response) throws DecisionResultInvalidException {
        if (response.isEmpty()) {
            if (_minimum == 0)
                return Collections.emptySet();
            else
                throw new DecisionResultInvalidException("No cards selected");
        }
        String[] cardIds = response.split(",");
        if (cardIds.length < _minimum || cardIds.length > _maximum)
            throw new DecisionResultInvalidException();

        Set<PhysicalCard> result = new HashSet<>();
        try {
            for (String cardId : cardIds) {
                PhysicalCard card = getSelectedCardById(Integer.parseInt(cardId));
                if (result.contains(card))
                    throw new DecisionResultInvalidException();
                result.add(card);
            }
        } catch (NumberFormatException e) {
            throw new DecisionResultInvalidException("No valid card ids matching " + response);
        }

        return result;
    }

    private PhysicalCard getSelectedCardById(int cardId) throws DecisionResultInvalidException {
        for (PhysicalCard physicalCard : _physicalCards)
            if (physicalCard.getCardId() == cardId)
                return physicalCard;

        throw new DecisionResultInvalidException();
    }

    public List<? extends PhysicalCard> getCardOptions() { return _physicalCards; }

    public void decisionMade(PhysicalCard card) throws DecisionResultInvalidException {
        decisionMade(String.valueOf(card.getCardId()));
    }

    public void decisionMade(List<PhysicalCard> cards) throws DecisionResultInvalidException {
        StringJoiner sj = new StringJoiner(",");
        for (PhysicalCard card : cards) {
            sj.add(String.valueOf(card.getCardId()));
        }
        decisionMade(sj.toString());
    }

}
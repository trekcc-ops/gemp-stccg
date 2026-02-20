package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class CardsSelectionDecision extends AbstractAwaitingDecision implements CardSelectionDecision {
    private final List<? extends PhysicalCard> _physicalCards;

    @JsonProperty("min")
    private final int _minimum;

    @JsonProperty("max")
    private final int _maximum;

    private final String[] _cardIds;

    @JsonProperty("independentlySelectable")
    private final boolean _independentlySelectable = true;

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  DefaultGame cardGame) {
        this(player, text, physicalCards, 0, physicalCards.size(), cardGame);
    }

    public CardsSelectionDecision(String playerName, String text, Collection<? extends PhysicalCard> physicalCards,
                                  int minimum, int maximum, DefaultGame cardGame) {
        super(playerName, text, cardGame);
        _physicalCards = new LinkedList<PhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        _cardIds = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            _cardIds[i] = String.valueOf(_physicalCards.get(i).getCardId());
    }

    public CardsSelectionDecision(Player player, String text, Collection<? extends PhysicalCard> physicalCards,
                                  int minimum, int maximum, DefaultGame cardGame) {
        super(player, text, cardGame);
        _physicalCards = new LinkedList<PhysicalCard>(physicalCards);
        _minimum = minimum;
        _maximum = maximum;
        _cardIds = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            _cardIds[i] = String.valueOf(_physicalCards.get(i).getCardId());
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

    public void decisionMade(PhysicalCard card) throws DecisionResultInvalidException {
        decisionMade(String.valueOf(card.getCardId()));
    }

    public void decisionMade(List<? extends PhysicalCard> cards) throws DecisionResultInvalidException {
        StringJoiner sj = new StringJoiner(",");
        for (PhysicalCard card : cards) {
            sj.add(String.valueOf(card.getCardId()));
        }
        decisionMade(sj.toString());
    }

    @JsonProperty("displayedCards")
    private List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (PhysicalCard card : _physicalCards) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", String.valueOf(card.getCardId()));
            mapToAdd.put("selectable", true);
            result.add(mapToAdd);
        }
        return result;
    }

    public List<? extends PhysicalCard> getSelectableCards() {
        return _physicalCards;
    }

}
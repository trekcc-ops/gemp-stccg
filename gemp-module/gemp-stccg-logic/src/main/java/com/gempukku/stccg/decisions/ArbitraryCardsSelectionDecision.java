package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class ArbitraryCardsSelectionDecision extends AbstractAwaitingDecision {
    private final List<PhysicalCard> _physicalCards = new LinkedList<>();
    private final Collection<? extends PhysicalCard> _selectable;
    private final int _minimum;
    private final int _maximum;
    private Map<String, List<String>> _validCombinations;

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
        super(player, text, AwaitingDecisionType.ARBITRARY_CARDS, cardGame);
        _physicalCards.addAll(physicalCards);
        _selectable = selectable;
        _minimum = minimum;
        _maximum = maximum;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getCardIds(physicalCards));
        setParam("blueprintId", getBlueprintIds(physicalCards));
        setParam("imageUrl", getImageUrls(physicalCards));
        setParam("selectable", getSelectable(physicalCards, selectable));
    }

    public ArbitraryCardsSelectionDecision(Player player, String text,
                                           Collection<? extends PhysicalCard> physicalCards,
                                           Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                           int minimum, int maximum, DefaultGame cardGame) {
        super(player, text, AwaitingDecisionType.CARD_SELECTION_FROM_COMBINATIONS, cardGame);
        _physicalCards.addAll(physicalCards);
        _selectable = physicalCards;
        _minimum = minimum;
        _maximum = maximum;
        _validCombinations = new HashMap<>();

        try {
            for (PersonnelCard personnel : validCombinations.keySet()) {
                String cardId = getCardIdForCard(personnel);
                List<String> pairingsList = new LinkedList<>();
                for (PersonnelCard pairing : validCombinations.get(personnel)) {
                    pairingsList.add(getCardIdForCard(pairing));
                }
                _validCombinations.put(cardId, pairingsList);
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
        }

        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getCardIds(physicalCards));
        setParam("blueprintId", getBlueprintIds(physicalCards));
        setParam("imageUrl", getImageUrls(physicalCards));
        setParam("selectable", getSelectable(physicalCards, physicalCards));
    }



    private String[] getSelectable(Collection<? extends PhysicalCard> physicalCards,
                                   Collection<? extends PhysicalCard> selectable) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = String.valueOf(selectable.contains(physicalCard));
            index++;
        }
        return result;
    }

    private String[] getCardIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = "temp" + index;
            index++;
        }
        return result;
    }

    // Only used for testing
    public String getCardIdForCard(PhysicalCard card) throws InvalidGameLogicException {
        int index = 0;
        for (PhysicalCard physicalCard : _physicalCards) {
            if (card == physicalCard)
                return "temp" + index;
            index++;
        }
        throw new InvalidGameLogicException("Card not found in decision");
    }

    private String[] getBlueprintIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = physicalCard.getBlueprintId();
            index++;
        }
        return result;
    }

    private String[] getImageUrls(Collection<? extends PhysicalCard> physicalCards) {
        String[] images = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            images[index] = physicalCard.getImageUrl();
            index++;
        }
        return images;
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
                int idNum = Integer.parseInt(cardId.substring(4));
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

    public void decisionMade(PhysicalCard card) throws DecisionResultInvalidException {
        List<PhysicalCard> cardList = new LinkedList<>();
        cardList.add(card);
        decisionMade(cardList);
    }


    public void decisionMade(List<PhysicalCard> cards) throws DecisionResultInvalidException {
        StringJoiner sj = new StringJoiner(",");
        for (PhysicalCard card : cards) {
            if (_physicalCards.contains(card))
                sj.add("temp" + _physicalCards.indexOf(card));
            else throw new DecisionResultInvalidException(
                    "Could not find card " + card.getCardId() + " in decision parameters");
        }
        decisionMade(sj.toString());
    }

    public String getValidCombinations() throws JsonProcessingException {
        if (_validCombinations == null)
            return null;
        else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(_validCombinations);
        }
    }

    public Map<String, List<String>> getValidCombinationsMap() {
        return _validCombinations;
    }

}
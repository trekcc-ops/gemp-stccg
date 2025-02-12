package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleCollectionPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final Map<String, List<String>> _cardsMap = new HashMap<>();
    private final List<DraftChoice> _draftChoices = new ArrayList<>();

    public SingleCollectionPickDraftChoiceDefinition(
            @JsonProperty("possiblePicks")
            List<SingleCollectionPickDraftChoice> draftChoices
    ) {
        for (SingleCollectionPickDraftChoice choice : draftChoices) {
            _draftChoices.addAll(draftChoices);
            _cardsMap.put(choice.getChoiceId(), choice.getCardIds());
        }
    }

    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        return _draftChoices;
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        List<String> cardIds = _cardsMap.get(choiceId);
        DefaultCardCollection cardCollection = new DefaultCardCollection();
        if (cardIds != null)
            for (String cardId : cardIds)
                cardCollection.addItem(cardId, 1);

        return cardCollection;
    }

}
package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.ArrayList;
import java.util.List;

public class MultipleCardPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final int _count;
    private final List<String> _cards;

    public MultipleCardPickDraftChoiceDefinition(
            @JsonProperty("count")
            int count,
            @JsonProperty("availableCards")
            List<String> cards
    ) {
        _count = count;
        _cards = cards;
    }

    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        final List<String> shuffledCards = getShuffledCards(seed, stage);

        List<DraftChoice> eligibleCards = new ArrayList<>(_count);
        for (int i = 0; i < _count; i++) {
            final int finalI = i;
            eligibleCards.add(
                    new DraftChoice() {
                        @Override
                        public String getChoiceId() {
                            return shuffledCards.get(finalI);
                        }

                        @Override
                        public String getBlueprintId() {
                            return shuffledCards.get(finalI);
                        }

                        @Override
                        public String getChoiceUrl() {
                            return null;
                        }
                    });
        }
        return eligibleCards;
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        List<String> shuffledCards = getShuffledCards(seed, stage);

        for (int i = 0; i < _count; i++) {
            if (shuffledCards.get(i).equals(choiceId)) {
                DefaultCardCollection result = new DefaultCardCollection();
                result.addItem(choiceId, 1);
                return result;
            }
        }

        return new DefaultCardCollection();
    }

    private List<String> getShuffledCards(long seed, int stage) {
        return TextUtils.getRandomizedList(_cards, getRandom(seed, stage));
    }

}
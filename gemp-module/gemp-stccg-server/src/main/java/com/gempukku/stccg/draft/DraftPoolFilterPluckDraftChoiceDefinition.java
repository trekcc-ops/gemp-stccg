package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DraftPoolFilterPluckDraftChoiceDefinition implements DraftChoiceDefinition {

    private final int _optionCount;
    private final String _filter;
    @JacksonInject(value = "cardLibrary")
    private CardBlueprintLibrary _cardLibrary;

    @JacksonInject(value = "formatLibrary")
    private FormatLibrary _formatLibrary;

    public DraftPoolFilterPluckDraftChoiceDefinition(
            @JsonProperty("optionCount")
            int optionCount,
            @JsonProperty("filter")
            String filter
    ) {
        _optionCount = optionCount;
        _filter = filter;
    }

    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        List<GenericCardItem> fullDraftPool = new ArrayList<>();
        for (GenericCardItem item : draftPool.getAll())
            for (int i = 0; i < draftPool.getItemCount(item.getBlueprintId()); i++)
                fullDraftPool.add(item);

        List<GenericCardItem> possibleCards =
                SortAndFilterCards.process(_filter, fullDraftPool, _cardLibrary, _formatLibrary);

        final List<GenericCardItem> cards = getCards(seed, stage, possibleCards);

        List<DraftChoice> draftChoices = new ArrayList<>(_optionCount);
        for (int i = 0; i < Math.min(_optionCount, possibleCards.size()); i++) {
            draftPool.removeItem(cards.get(i).getBlueprintId(),1);
            final int finalI = i;
            draftChoices.add(
                    new DraftChoice() {
                        @Override
                        public String getChoiceId() {
                            return cards.get(finalI).getBlueprintId();
                        }

                        @Override
                        public String getBlueprintId() {
                            return cards.get(finalI).getBlueprintId();
                        }

                        @Override
                        public String getChoiceUrl() {
                            return null;
                        }
                    });
        }
        return draftChoices;
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        DefaultCardCollection cardCollection = new DefaultCardCollection();
        cardCollection.addItem(choiceId, 1);
        return cardCollection;
    }

    private List<GenericCardItem> getCards(long seed, int stage, List<GenericCardItem> possibleCards) {
        Random rnd = getRandom(seed, stage);
        // Fixing some weird issue with Random
        rnd.nextInt();
        Collections.shuffle(possibleCards, rnd);
        return possibleCards;
    }
}
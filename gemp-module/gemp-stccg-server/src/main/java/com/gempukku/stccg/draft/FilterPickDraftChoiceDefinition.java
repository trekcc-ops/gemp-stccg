package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CompleteCardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.List;

public class FilterPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final int _optionCount;
    private final String _filter;
    @JacksonInject("cardLibrary")
    private CardBlueprintLibrary _cardLibrary;
    @JacksonInject("formatLibrary")
    private FormatLibrary _formatLibrary;

    public FilterPickDraftChoiceDefinition(
        @JsonProperty("optionCount")
        int optionCount,
        @JsonProperty("filter")
        String filter
    ) {
        _optionCount = optionCount;
        _filter = filter.replace(" ", "|");
    }

    @JsonIgnore
    private List<GenericCardItem> getPossibleCards() {
        Iterable<GenericCardItem> cards = new CompleteCardCollection(_cardLibrary).getAll();
        return SortAndFilterCards.process(_filter, cards, _cardLibrary, _formatLibrary);
    }

    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        final List<GenericCardItem> cards = getCards(seed, stage);

        List<DraftChoice> draftChoices = new ArrayList<>(_optionCount);
        for (int i = 0; i < Math.min(_optionCount, getPossibleCards().size()); i++) {
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

    @JsonIgnore
    private List<GenericCardItem> getCards(long seed, int stage) {
        return TextUtils.getRandomizedList(getPossibleCards(), getRandom(seed, stage));
    }

}
package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FilterPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final int _optionCount;
    private final String _filter;
    private List<GenericCardItem> _possibleCards = new LinkedList<>();

    public FilterPickDraftChoiceDefinition(
        @JsonProperty("optionCount")
        int optionCount,
            @JsonProperty("filter")
            String filter
    ) {
        _optionCount = optionCount;
        _filter = filter.replace(" ","|");
    }

    public FilterPickDraftChoiceDefinition(JsonNode node) {
        _optionCount = node.get("optionCount").intValue();
        _filter = node.get("filter").textValue().replace(" ", "|");
    }

    public void assignPossibleCards(CollectionsManager collectionsManager,
                                                   CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary) {
        _possibleCards = SortAndFilterCards.process(_filter, collectionsManager.getCompleteCardCollection().getAll(),
                cardLibrary, formatLibrary);
    }

    @Override
    public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                          DefaultCardCollection draftPool) {
        final List<GenericCardItem> cards = getCards(seed, stage);

        List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>(_optionCount);
        for (int i = 0; i < Math.min(_optionCount, _possibleCards.size()); i++) {
            final int finalI = i;
            draftChoices.add(
                    new SoloDraft.DraftChoice() {
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

    private List<GenericCardItem> getCards(long seed, int stage) {
        return TextUtils.getRandomizedList(_possibleCards, getRandom(seed, stage));
    }

}
package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
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

public class DraftPoolFilterPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final int _optionCount;
    private final String _filter;
    private final CardBlueprintLibrary _cardLibrary;
    private final FormatLibrary _formatLibrary;

    public DraftPoolFilterPickDraftChoiceDefinition(JsonNode data, CardBlueprintLibrary cardLibrary,
                                                    FormatLibrary formatLibrary) {
        _optionCount = data.get("optionCount").asInt();
        _filter = data.get("filter").textValue();
        _cardLibrary = cardLibrary;
        _formatLibrary = formatLibrary;
    }

    @Override
    public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                          DefaultCardCollection draftPool) {
        List<GenericCardItem> possibleCards = SortAndFilterCards.process(
                _filter, draftPool.getAll(), _cardLibrary, _formatLibrary);

        final List<GenericCardItem> cards = getCards(seed, stage, possibleCards);

        List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>(_optionCount);
        for (int i = 0; i < Math.min(_optionCount, possibleCards.size()); i++) {
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

    private List<GenericCardItem> getCards(long seed, int stage, List<GenericCardItem> possibleCards) {
        Random rnd = getRandom(seed, stage);
        // Fixing some weird issue with Random
        rnd.nextInt();
        Collections.shuffle(possibleCards, rnd);
        return possibleCards;
    }

}
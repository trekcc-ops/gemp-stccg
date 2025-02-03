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
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.*;

public class SingleCollectionPickDraftChoiceDefinition implements DraftChoiceDefinition {

    private final Map<String, List<String>> _cardsMap = new HashMap<>();
    private final List<SoloDraft.DraftChoice> _draftChoices = new ArrayList<>();

    public SingleCollectionPickDraftChoiceDefinition(JsonNode node) {
        List<JsonNode> switchResult = JsonUtils.toArray(node.get("possiblePicks"));

        for (JsonNode pickDefinition : switchResult) {
            final String choiceId = pickDefinition.get("choiceId").textValue();
            final String url = pickDefinition.get("url").textValue();
            List<String> cardIds = JsonUtils.toStringArray(pickDefinition.get("cards"));

            _draftChoices.add(
                    new SoloDraft.DraftChoice() {
                        @Override
                        public String getChoiceId() {
                            return choiceId;
                        }

                        @Override
                        public String getBlueprintId() {
                            return null;
                        }

                        @Override
                        public String getChoiceUrl() {
                            return url;
                        }
                    });
            _cardsMap.put(choiceId, cardIds);
        }
    }

    @Override
    public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
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
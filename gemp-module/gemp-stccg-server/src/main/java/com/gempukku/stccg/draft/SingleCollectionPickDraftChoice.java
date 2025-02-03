package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SingleCollectionPickDraftChoice implements DraftChoice {

    private final String _choiceId;
    private final String _url;
    private final List<String> _cardIds;

    SingleCollectionPickDraftChoice(
            @JsonProperty(value = "choiceId", required = true)
            String choiceId,
            @JsonProperty(value = "url", required = true)
            String url,
            @JsonProperty(value = "cardIds", required = true)
            List<String> cardIds
    ) {
        _choiceId = choiceId;
        _url = url;
        _cardIds = Objects.requireNonNullElse(cardIds, new LinkedList<>());
    }

    @Override
    public String getChoiceId() {
        return _choiceId;
    }

    @Override
    public String getBlueprintId() {
        return null;
    }

    @Override
    public String getChoiceUrl() {
        return _url;
    }

    public List<String> getCardIds() {
        return _cardIds;
    }
}
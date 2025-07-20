package com.gempukku.stccg.decisions.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CardSelectionDecisionResponse implements DecisionResponse {

    @JsonProperty("decisionId")
    private int _decisionId;

    @JsonProperty("cardIds")
    private List<Integer> _cardIds;

    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    public List<Integer> getCardIds() {
        return _cardIds;
    }
}
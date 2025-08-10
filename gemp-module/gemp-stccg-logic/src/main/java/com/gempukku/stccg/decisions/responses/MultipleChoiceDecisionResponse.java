package com.gempukku.stccg.decisions.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MultipleChoiceDecisionResponse implements DecisionResponse {

    @JsonProperty("decisionId")
    private int _decisionId;

    @JsonProperty("responseIndex")
    private int _responseIndex;

    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    public int getResponseIndex() {
        return _responseIndex;
    }
}
package com.gempukku.stccg.decisions.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MultipleChoiceDecisionResponse implements DecisionResponse {

    @JsonProperty("decisionId")
    private int _decisionId;

    @JsonProperty("actionIds")
    private int _responseIndex;

    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    public int getResponseIndex() {
        return _responseIndex;
    }
}
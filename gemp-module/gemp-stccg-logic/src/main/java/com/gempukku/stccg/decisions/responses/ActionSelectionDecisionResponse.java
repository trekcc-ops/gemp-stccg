package com.gempukku.stccg.decisions.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ActionSelectionDecisionResponse implements DecisionResponse {

    @JsonProperty("decisionId")
    private int _decisionId;

    @JsonProperty("actionIds")
    private List<Integer> _actionIds;

    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    public List<Integer> getActionIds() {
        return _actionIds;
    }
}
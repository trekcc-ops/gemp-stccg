package com.gempukku.stccg.decisions.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerSelectionDecisionResponse implements DecisionResponse {

    @JsonProperty("decisionId")
    private int _decisionId;

    @JsonProperty("selectedValue")
    private Integer _selectedValue;

    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    public Integer getSelectedValue() {
        return _selectedValue;
    }
}
package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

import java.util.Map;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="decisionId")
public interface AwaitingDecision {
    @JsonProperty("decisionId")
    int getDecisionId();

    String getText();

    AwaitingDecisionType getDecisionType();

    Map<String, String[]> getDecisionParameters();

    void decisionMade(String result) throws DecisionResultInvalidException;

    String getDecidingPlayerId();
}
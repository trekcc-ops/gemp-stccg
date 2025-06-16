package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

import java.util.Collection;
import java.util.Map;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="decisionId")
@JsonIncludeProperties({ "decisionId", "decisionType", "text", "min", "max", "defaultValue", "results",
        "validCombinations", "cardIds", "displayedCards", "context", "elementType", "actions" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface AwaitingDecision {
    @JsonProperty("decisionId")
    int getDecisionId();

    @JsonProperty("text")
    String getText();

    @JsonProperty("decisionType")
    AwaitingDecisionType getDecisionType();

    @JsonProperty("elementType")
    String getElementType();

    void decisionMade(String result) throws DecisionResultInvalidException;

    String getDecidingPlayerId();

}
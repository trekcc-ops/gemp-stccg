package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="decisionId")
@JsonIncludeProperties({ "decisionId", "text", "min", "max", "options",
        "displayedCards", "context", "elementType", "actions", "independentlySelectable" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface AwaitingDecision {
    @JsonProperty("decisionId")
    int getDecisionId();

    @JsonProperty("text")
    String getText();

    @JsonProperty("elementType")
    String getElementType();

    void decisionMade(String result) throws DecisionResultInvalidException;

    String getDecidingPlayerId();

}
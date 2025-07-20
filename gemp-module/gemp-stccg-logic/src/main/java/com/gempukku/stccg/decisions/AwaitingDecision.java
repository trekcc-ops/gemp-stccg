package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

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

    String getDecidingPlayerId();

    void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException;

    void followUp() throws DecisionResultInvalidException, InvalidGameLogicException;

}
package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Map;

//@JsonSerialize(using = AwaitingDecisionSerializer.class)
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="decisionId")
public interface AwaitingDecision {
    @JsonProperty("decisionId")
    int getAwaitingDecisionId();

    String getText();

    AwaitingDecisionType getDecisionType();

    Map<String, String[]> getDecisionParameters();

    void decisionMade(String result) throws DecisionResultInvalidException;
    Player getDecidingPlayer(DefaultGame game);
    String getDecidingPlayerId();
}
package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Map;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="decisionId")
public interface AwaitingDecision {
    @JsonProperty("decisionId")
    int getDecisionId();

    String getText();

    AwaitingDecisionType getDecisionType();

    Map<String, String[]> getDecisionParameters();

    void decisionMade(String result) throws DecisionResultInvalidException;
    Player getDecidingPlayer(DefaultGame game) throws PlayerNotFoundException;
    String getDecidingPlayerId();
}
package com.gempukku.stccg.decisions;

import java.util.Set;

public interface UserFeedback {
    void sendAwaitingDecision(AwaitingDecision awaitingDecision);
    boolean hasNoPendingDecisions();
    AwaitingDecision getAwaitingDecision(String playerName);
    void removeDecision(String playerId);
    Set<String> getUsersPendingDecision();
}
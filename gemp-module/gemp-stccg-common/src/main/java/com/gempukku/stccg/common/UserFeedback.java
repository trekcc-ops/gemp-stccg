package com.gempukku.stccg.common;

import java.util.Set;

public interface UserFeedback {
    void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision);
    boolean hasNoPendingDecisions();
    AwaitingDecision getAwaitingDecision(String playerName);
    void removeDecision(String playerId);
    Set<String> getUsersPendingDecision();
}

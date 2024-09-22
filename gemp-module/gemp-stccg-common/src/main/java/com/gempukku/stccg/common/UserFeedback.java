package com.gempukku.stccg.common;

public interface UserFeedback {
    void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision);

    boolean hasNoPendingDecisions();
}

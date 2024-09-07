package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.decisions.AwaitingDecision;

public interface UserFeedback {
    void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision);

    boolean hasNoPendingDecisions();
}

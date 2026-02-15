package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Set;

public interface UserFeedback {
    DefaultGame getGame();
    GameState getGameState();

    default void removeDecision(String playerName) {
        getGameState().removeDecision(playerName);
    }
    default AwaitingDecision getAwaitingDecision(String playerName) {
        return getGameState().getDecision(playerName);
    }
    default void addPendingDecision(AwaitingDecision decision) {
        getGameState().addPendingDecision(decision);
    }

    default void sendAwaitingDecision(AwaitingDecision awaitingDecision) {
        addPendingDecision(awaitingDecision);
        getGame().sendActionResultToClient();
    }

    default Set<String> getUsersPendingDecision() {
        return getGameState().getUsersPendingDecision();
    }

    default int getNextDecisionIdAndIncrement() {
        return getGameState().getAndIncrementNextDecisionId();
    }

}
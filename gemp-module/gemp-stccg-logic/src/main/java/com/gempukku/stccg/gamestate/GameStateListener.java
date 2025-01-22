package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

public interface GameStateListener {
    String getPlayerId();
    void sendEvent(GameEvent gameEvent);
    void sendEvent(GameEvent.Type eventType);

    void initializeBoard() throws PlayerNotFoundException;

    void setCurrentPhase(Phase phase);

    void setPlayerDecked(Player player);
    void setPlayerScore(String playerId);

    void setTribbleSequence(String tribbleSequence);

    void setCurrentPlayerId(String playerId);

    void sendMessage(String message);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision) throws PlayerNotFoundException;

    void sendWarning(String playerId, String warning);
    long getLastAccessed();
    int getChannelNumber();
}
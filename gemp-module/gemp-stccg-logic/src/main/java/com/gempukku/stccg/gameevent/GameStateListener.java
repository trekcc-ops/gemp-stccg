package com.gempukku.stccg.gameevent;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public interface GameStateListener {
    String getPlayerId();
    void sendEvent(GameEvent gameEvent);

    void initializeBoard() throws PlayerNotFoundException;

    void setTribbleSequence(String tribbleSequence);

    void sendMessage(String message);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision) throws PlayerNotFoundException;

    void sendWarning(String playerId, String warning);
    long getLastAccessed();
    int getChannelNumber();
}
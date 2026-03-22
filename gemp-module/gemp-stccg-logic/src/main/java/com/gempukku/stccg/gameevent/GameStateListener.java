package com.gempukku.stccg.gameevent;

import java.time.ZonedDateTime;

public interface GameStateListener {
    String getPlayerId();
    void sendEvent(GameEvent gameEvent);

    void sendMessageEvent(String message);

    void sendWarning(String playerId, String warning);

    int getChannelNumber();
    ZonedDateTime getLastAccessed();
}
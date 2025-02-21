package com.gempukku.stccg.gameevent;

public interface GameStateListener {
    String getPlayerId();
    void sendEvent(GameEvent gameEvent);

    void sendMessageEvent(String message);

    void sendWarning(String playerId, String warning);
    long getLastAccessed();
    int getChannelNumber();
}
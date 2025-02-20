package com.gempukku.stccg.gameevent;

public interface GameStateListener {
    String getPlayerId();
    void sendEvent(GameEvent gameEvent);

    void sendMessage(String message);

    void sendWarning(String playerId, String warning);
    long getLastAccessed();
    int getChannelNumber();
}
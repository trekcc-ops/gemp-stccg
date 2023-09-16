package com.gempukku.lotro.chat;

public interface ChatCommandCallback {
    void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException;
}

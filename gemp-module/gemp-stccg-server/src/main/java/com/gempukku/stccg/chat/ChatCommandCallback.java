package com.gempukku.stccg.chat;

public interface ChatCommandCallback {
    void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException;
}

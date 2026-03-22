package com.gempukku.stccg.chat;

interface ChatCommandCallback {
    void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException;
}
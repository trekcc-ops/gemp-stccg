package com.gempukku.stccg.chat;

import com.gempukku.stccg.database.UserNotFoundException;

interface ChatCommandCallback {
    void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException;
}
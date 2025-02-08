package com.gempukku.stccg.chat;

import com.gempukku.stccg.database.User;

interface ChatRoomListener {
    void messageReceived(ChatMessage message);
    User getUser();
}
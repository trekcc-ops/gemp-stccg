package com.gempukku.stccg.chat;

import com.gempukku.stccg.AbstractServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends AbstractServer {

    private final Map<String, ChatRoomMediator> _chatRooms = new ConcurrentHashMap<>();

    public void addChatRoom(ChatRoomMediator chatRoom) {
        _chatRooms.put(chatRoom.getName(), chatRoom);
    }


    public final void sendSystemMessageToAllUsers(String message) {
        // Sends the message to all users in all chat rooms
        String messageText = ChatStrings.ALL_USERS_PREFIX + message;
        try {
            for (ChatRoomMediator mediator : _chatRooms.values())
                mediator.sendChatMessage(ChatStrings.SYSTEM_USER_ID, messageText, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
    }


    public final ChatRoomMediator getChatRoom(String name) {
        return _chatRooms.get(name);
    }

    protected final void cleanup() {
        for (ChatRoomMediator chatRoomMediator : _chatRooms.values())
            chatRoomMediator.cleanup();
        _chatRooms.values().removeIf(ChatRoomMediator::isDestroyed);
    }
}
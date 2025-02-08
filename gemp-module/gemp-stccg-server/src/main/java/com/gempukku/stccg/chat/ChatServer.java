package com.gempukku.stccg.chat;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.ServerObjects;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends AbstractServer {
    private final ServerObjects _serverObjects;
    private final Map<String, ChatRoomMediator> _chatRooms = new ConcurrentHashMap<>();

    public ChatServer(ServerObjects serverObjects) {
        _serverObjects = serverObjects;
    }

    public void addChatRoom(ChatRoomMediator chatRoom) {
        _chatRooms.put(chatRoom.getName(), chatRoom);
    }


    public final void createChatRoom(String name, boolean muteJoinPartMessages, int secondsTimeoutPeriod,
                                     boolean allowIncognito) {
        ChatRoomMediator chatRoom = new ChatRoomMediator(_serverObjects, muteJoinPartMessages,
                secondsTimeoutPeriod, allowIncognito, null, name);
        try {
            chatRoom.sendMessage(ChatStrings.SYSTEM_USER_ID, "Welcome to room: " + name, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        addChatRoom(chatRoom);
    }


    public final void createPrivateChatRoom(String name, boolean muteJoinPartMessages, Set<String> allowedUsers,
                                            int secondsTimeoutPeriod) {
        ChatRoomMediator chatRoom = new ChatRoomMediator(_serverObjects, muteJoinPartMessages,
                secondsTimeoutPeriod, allowedUsers, false, name);
        try {
            chatRoom.sendMessage(ChatStrings.SYSTEM_USER_ID, "Welcome to private room: " + name, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        addChatRoom(chatRoom);
    }

    public final void sendSystemMessageToAllUsers(String message) {
        // Sends the message to all users in all chat rooms
        String messageText = ChatStrings.ALL_USERS_PREFIX + message;
        try {
            for (ChatRoomMediator mediator : _chatRooms.values())
                mediator.sendMessage(ChatStrings.SYSTEM_USER_ID, messageText, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
    }

    public final ChatRoomMediator getChatRoom(String name) {
        return _chatRooms.get(name);
    }

    public final void destroyChatRoom(String name) {
        _chatRooms.remove(name);
    }

    protected final void cleanup() {
        for (ChatRoomMediator chatRoomMediator : _chatRooms.values())
            chatRoomMediator.cleanup();
    }
}
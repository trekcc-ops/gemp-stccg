package com.gempukku.stccg.chat;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.PrivateInformationException;
import com.gempukku.stccg.db.IgnoreDAO;
import com.gempukku.stccg.db.PlayerDAO;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends AbstractServer {
    private final IgnoreDAO ignoreDAO;
    private final PlayerDAO playerDAO;
    private final Map<String, ChatRoomMediator> _chatRooms = new ConcurrentHashMap<>();

    public ChatServer(IgnoreDAO ignoreDAO, PlayerDAO playerDAO) {
        this.ignoreDAO = ignoreDAO;
        this.playerDAO = playerDAO;
    }

    public ChatRoomMediator createChatRoom(String name, boolean muteJoinPartMessages, int secondsTimeoutPeriod, boolean allowIncognito, String welcomeMessage) {
        ChatRoomMediator chatRoom = new ChatRoomMediator(ignoreDAO, playerDAO, muteJoinPartMessages,
                secondsTimeoutPeriod, allowIncognito, welcomeMessage);
        try {
            chatRoom.sendMessage("System", "Welcome to room: " + name, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        _chatRooms.put(name, chatRoom);
        return chatRoom;
    }

    public void createPrivateChatRoom(String name, boolean muteJoinPartMessages, Set<String> allowedUsers, int secondsTimeoutPeriod) {
        ChatRoomMediator chatRoom = new ChatRoomMediator(ignoreDAO, playerDAO, muteJoinPartMessages, secondsTimeoutPeriod, allowedUsers, false);
        try {
            chatRoom.sendMessage("System", "Welcome to private room: " + name, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        _chatRooms.put(name, chatRoom);
    }

    public void sendSystemMessageToAllChatRooms(String message) {
        try {
            for (ChatRoomMediator chatRoomMediator : _chatRooms.values())
                chatRoomMediator.sendMessage("System", message, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
    }

    public ChatRoomMediator getChatRoom(String name) {
        return _chatRooms.get(name);
    }

    public void destroyChatRoom(String name) {
        _chatRooms.remove(name);
    }

    protected void cleanup() {
        for (ChatRoomMediator chatRoomMediator : _chatRooms.values())
            chatRoomMediator.cleanup();
    }
}

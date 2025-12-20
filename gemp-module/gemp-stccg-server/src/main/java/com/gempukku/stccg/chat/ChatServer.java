package com.gempukku.stccg.chat;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.service.AdminService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends AbstractServer {
    private final AdminService _adminService;
    private final Map<String, ChatRoomMediator> _chatRooms = new ConcurrentHashMap<>();

    public ChatServer(AdminService adminService) {
        _adminService = adminService;
    }

    public void addChatRoom(ChatRoomMediator chatRoom) {
        _chatRooms.put(chatRoom.getName(), chatRoom);
    }


    public final void createChatRoom(String name, boolean muteJoinPartMessages, Set<String> allowedUsers,
                                     int secondsTimeoutPeriod, boolean isPrivate) {
        String welcomeMessage = (isPrivate) ? "Welcome to private room" : "Welcome to room";
        ChatRoomMediator chatRoom = new ChatRoomMediator(_adminService, muteJoinPartMessages,
                secondsTimeoutPeriod, allowedUsers, false, name);
        try {
            chatRoom.sendChatMessage(ChatStrings.SYSTEM_USER_ID, welcomeMessage + ": " + name, true);
        } catch (PrivateInformationException | ChatCommandErrorException exp) {
            // Ignore, sent as admin
        }
        addChatRoom(chatRoom);
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

    public final void destroyChatRoom(String name) {
        _chatRooms.remove(name);
    }

    protected final void cleanup() {
        for (ChatRoomMediator chatRoomMediator : _chatRooms.values())
            chatRoomMediator.cleanup();
    }
}
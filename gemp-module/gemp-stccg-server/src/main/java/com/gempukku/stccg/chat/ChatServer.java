package com.gempukku.stccg.chat;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.hall.GameSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends AbstractServer {
    private static final int TIMEOUT_PERIOD = 30;

    private final Map<String, ChatRoomMediator> _chatRooms = new ConcurrentHashMap<>();

    public void addChatRoom(ChatRoomMediator chatRoom) {
        _chatRooms.put(chatRoom.getName(), chatRoom);
    }


    public void createGameChatRoom(GameSettings gameSettings, GameParticipant[] participants, String gameId) {
        String chatRoomName = getChatRoomName(gameId);
        Set<String> allowedUsers = new HashSet<>();
        boolean isCompetitive = gameSettings.isCompetitive();

        if (isCompetitive) {
            for (GameParticipant participant : participants)
                allowedUsers.add(participant.getPlayerId());
        }

        ChatRoomMediator chatRoom = getChatRoomMediator(isCompetitive, allowedUsers, chatRoomName);
        addChatRoom(chatRoom);
    }

    @NotNull
    private ChatRoomMediator getChatRoomMediator(boolean isCompetitive, Set<String> allowedUsers, String chatRoomName) {
        String welcomeMessage = isCompetitive ? "Welcome to private room" : "Welcome to room";
        ChatRoomMediator chatRoom = new ChatRoomMediator(false,
                TIMEOUT_PERIOD, allowedUsers, false, chatRoomName);
        try {
            chatRoom.sendChatMessage(ChatStrings.SYSTEM_USER_ID,
                    welcomeMessage + ": " + chatRoomName, true);
        } catch (PrivateInformationException | ChatCommandErrorException exp) {
            // Ignore, sent as admin
        }
        return chatRoom;
    }

    private static String getChatRoomName(String gameId) {
        return "Game" + gameId;
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
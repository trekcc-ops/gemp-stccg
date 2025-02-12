package com.gempukku.stccg.chat;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.database.User;

import java.util.*;


// Json serialization in this class is used to send updates to the client

@JsonIncludeProperties({ "roomName", "users", "messages" })
public class ChatCommunicationChannel implements ChatRoomListener, LongPollableResource {
    private List<ChatMessage> _messages = new LinkedList<>();
    private long _lastConsumed = System.currentTimeMillis();
    private volatile WaitingRequest _waitingRequest;
    private final Set<String> _ignoredUsers;
    private final User _channelUser;

    private final ChatRoomMediator _chatRoom;

    ChatCommunicationChannel(ChatRoomMediator chatRoom, User user, Set<String> ignoredUsers) {
        _channelUser = user;
        _ignoredUsers = ignoredUsers;
        _chatRoom = chatRoom;
    }

    @Override
    public final synchronized void deregisterRequest() { _waitingRequest = null; }

    @Override
    public final synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (!_messages.isEmpty())
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    @Override
    public final synchronized void messageReceived(ChatMessage message) {
        String messageSender = message.getFrom();
        if (message.isFromAdmin() || !_ignoredUsers.contains(messageSender)) {
            _messages.add(message);
            if (_waitingRequest != null) {
                _waitingRequest.processRequest();
                _waitingRequest = null;
            }
        }
    }

    @JsonProperty("messages")
    public final synchronized List<ChatMessage> consumeMessages() {
        updateLastAccess();
        List<ChatMessage> messages = _messages;
        _messages = new LinkedList<>();
        return messages;
    }

    private synchronized void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    final synchronized long getLastAccessed() {
        return _lastConsumed;
    }

    @SuppressWarnings("unused")
    @JsonProperty("users")
    private Collection<Map<Object, Object>> getRoomUsers() {
        Collection<Map<Object, Object>> users = new ArrayList<>();
        boolean includeIncognito = _channelUser.isAdmin();
        for (User user : _chatRoom.getUsersInRoom(_channelUser.isAdmin())) {
            Map<Object, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("isAdmin", user.isAdmin());
            userInfo.put("isLeagueAdmin", user.isLeagueAdmin());
            users.add(userInfo);
        }
        return users;
    }

    @SuppressWarnings("unused")
    @JsonProperty("roomName")
    private String getRoomName() {
        return _chatRoom.getName();
    }
    
    public User getUser() {
        return _channelUser;
    }
}
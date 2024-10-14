package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChatCommunicationChannel implements ChatRoomListener, LongPollableResource {
    private List<ChatMessage> _messages = new LinkedList<>();
    private long _lastConsumed = System.currentTimeMillis();
    private volatile WaitingRequest _waitingRequest;
    private final Set<String> ignoredUsers;

    ChatCommunicationChannel(Set<String> ignoredUsers) {
        this.ignoredUsers = ignoredUsers;
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
        if (message.isFromAdmin() || !ignoredUsers.contains(messageSender)) {
            _messages.add(message);
            if (_waitingRequest != null) {
                _waitingRequest.processRequest();
                _waitingRequest = null;
            }
        }
    }

    public final synchronized List<ChatMessage> consumeMessages() {
        updateLastAccess();
        List<ChatMessage> messages = _messages;
        _messages = new LinkedList<>();
        return messages;
    }

    private synchronized void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public final synchronized long getLastAccessed() {
        return _lastConsumed;
    }
}
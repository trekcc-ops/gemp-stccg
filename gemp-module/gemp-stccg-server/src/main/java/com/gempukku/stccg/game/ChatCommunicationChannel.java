package com.gempukku.stccg.game;

import com.gempukku.stccg.chat.ChatMessage;
import com.gempukku.stccg.chat.ChatRoomListener;
import com.gempukku.stccg.common.LongPollableResource;
import com.gempukku.stccg.common.WaitingRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChatCommunicationChannel implements ChatRoomListener, LongPollableResource {
    private List<ChatMessage> _messages = new LinkedList<>();
    private long _lastConsumed = System.currentTimeMillis();
    private volatile WaitingRequest _waitingRequest;
    private final Set<String> ignoredUsers;

    public ChatCommunicationChannel(Set<String> ignoredUsers) {
        this.ignoredUsers = ignoredUsers;
    }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (!_messages.isEmpty())
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    @Override
    public synchronized void messageReceived(ChatMessage message) {
        if (message.isFromAdmin() || !ignoredUsers.contains(message.getFrom())) {
            _messages.add(message);
            if (_waitingRequest != null) {
                _waitingRequest.processRequest();
                _waitingRequest = null;
            }
        }
    }

    public synchronized List<ChatMessage> consumeMessages() {
        updateLastAccess();
        List<ChatMessage> messages = _messages;
        _messages = new LinkedList<>();
        return messages;
    }

    public synchronized boolean hasMessages() {
        updateLastAccess();
        return !_messages.isEmpty();
    }

    private synchronized void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public synchronized long getLastAccessed() {
        return _lastConsumed;
    }
}

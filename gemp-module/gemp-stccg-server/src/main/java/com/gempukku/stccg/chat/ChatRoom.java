package com.gempukku.stccg.chat;

import java.util.*;
import java.util.stream.Collectors;

class ChatRoom {
    private static final int MAX_MESSAGE_HISTORY_COUNT = 500;
    private final SequencedCollection<ChatMessage> _lastMessages = new LinkedList<>();
    private final Map<String, ChatRoomInfo> _chatRoomListeners = new TreeMap<>(
            String::compareToIgnoreCase);
    private final boolean _muteJoinPartMessages;
    private final boolean _allowIncognito;

    ChatRoom(boolean muteJoinPartMessages, boolean allowIncognito) {
        _muteJoinPartMessages = muteJoinPartMessages;
        _allowIncognito = allowIncognito;
    }

    final void setUserIncognitoMode(String username, boolean incognito) {
        if (_allowIncognito) {
            final ChatRoomInfo chatRoomInfo = _chatRoomListeners.get(username);
            if (chatRoomInfo != null)
                chatRoomInfo.incognito = incognito;
        }
    }

    final void postMessage(String from, String message, boolean fromAdmin) {
        ChatMessage chatMessage = new ChatMessage(from, message, fromAdmin);
        _lastMessages.add(chatMessage);
        shrinkLastMessages();

        for (Map.Entry<String, ChatRoomInfo> listeners : _chatRoomListeners.entrySet())
            listeners.getValue().chatRoomListener.messageReceived(chatMessage);
    }

    final void postToUser(String from, String to, String message) {
        ChatMessage chatMessage = new ChatMessage(from, message, false);
        final ChatRoomListener chatRoomListener = _chatRoomListeners.get(to).chatRoomListener;
        if (chatRoomListener != null) {
            chatRoomListener.messageReceived(chatMessage);
        }
    }

    final void joinChatRoom(String playerId, ChatRoomListener listener) {
        boolean wasInRoom = _chatRoomListeners.containsKey(playerId);
        _chatRoomListeners.put(playerId, new ChatRoomInfo(listener, false));
        for (ChatMessage lastMessage : _lastMessages)
            listener.messageReceived(lastMessage);
        if (!wasInRoom && !_muteJoinPartMessages) {
            String message = ChatStrings.userJoinedRoom(playerId);
            postMessage(ChatStrings.SYSTEM_USER_ID, message, false);
        }
    }

    final void partChatRoom(String playerId) {
        boolean wasInRoom = (_chatRoomListeners.remove(playerId) != null);
        if (wasInRoom && !_muteJoinPartMessages) {
            String message = ChatStrings.userLeftRoom(playerId);
            postMessage(ChatStrings.SYSTEM_USER_ID, message, false);
        }
    }

    final Collection<String> getUsersInRoom(boolean includeIncognito) {
        if (includeIncognito)
            return new ArrayList<>(_chatRoomListeners.keySet());
        else {
            return _chatRoomListeners.entrySet().stream().filter(
                    entry -> !entry.getValue().incognito).map(Map.Entry::getKey).collect(Collectors.toList());
        }
    }

    private void shrinkLastMessages() {
        while (_lastMessages.size() > MAX_MESSAGE_HISTORY_COUNT) {
            _lastMessages.removeFirst();
        }
    }

    private static class ChatRoomInfo {
        private final ChatRoomListener chatRoomListener;
        private boolean incognito;

        ChatRoomInfo(ChatRoomListener chatRoomListener, boolean incognito) {
            this.chatRoomListener = chatRoomListener;
            this.incognito = incognito;
        }
    }
}
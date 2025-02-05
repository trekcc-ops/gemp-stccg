package com.gempukku.stccg.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessage {
    private final String _from;
    private final String _message;
    private final boolean _fromAdmin;
    private final String _messageDate;

    ChatMessage(String from, String message, boolean fromAdmin) {
        long currentTime = System.currentTimeMillis();
        _messageDate = String.valueOf(currentTime);
        _from = from;
        _message = message;
        _fromAdmin = fromAdmin;
    }

    @JsonProperty("fromUser")
    public final String getFrom() {
        return _from;
    }

    @JsonProperty("timestamp")
    public final String getTimestamp() {
        return _messageDate;
    }

    @JsonProperty("userIsAdmin")
    final boolean isFromAdmin() {
        return _fromAdmin;
    }

    @JsonProperty("messageText")
    final String getMessageText() {
        return _message;
    }

}
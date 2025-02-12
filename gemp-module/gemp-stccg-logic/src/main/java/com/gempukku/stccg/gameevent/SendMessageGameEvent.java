package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageGameEvent extends GameEvent {

    @JsonProperty("message")
    private final String _message;

    public SendMessageGameEvent(GameEvent.Type eventType, String message) {
        super(eventType);
        _message = message;
        _eventAttributes.put(Attribute.message, message);
    }

}
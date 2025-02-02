package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SendMessageGameEvent extends GameEvent {

    @JacksonXmlProperty(localName = "message", isAttribute = true)
    private final String _message;

    public SendMessageGameEvent(GameEvent.Type eventType, String message) {
        super(eventType);
        _message = message;
    }

}
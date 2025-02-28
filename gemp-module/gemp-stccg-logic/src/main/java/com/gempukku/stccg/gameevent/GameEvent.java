package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {
    public enum Type {
        SEND_MESSAGE("M"),
        SEND_WARNING("W"),
        ACTION_RESULT("ACTION_RESULT");

        private final String code;

        Type(String code) {
            this.code = code;
        }

    }

    public enum Attribute {
        message,
        phase,
        timestamp,
        type
    }

    @JsonIgnore
    private final Type _type;

    @JsonProperty("type")
    private final String _typeCode;

    @JsonProperty("timeStamp")
    private final String _timeStamp;

    @JsonProperty("participantId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String _playerId;

    private Zone _zone;
    protected final Map<Attribute, String> _eventAttributes = new HashMap<>();

    public GameEvent(Type type) {
        _type = type;
        _typeCode = type.code;
        _eventAttributes.put(Attribute.type, type.code);
        _eventAttributes.put(Attribute.timestamp,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS")));
        _timeStamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS"));
    }


    @JsonIgnore
    public Type getType() { return _type; }

    public Zone getZone() throws InvalidGameOperationException { return _zone; }

    @JsonIgnore
    public String getAttribute(Attribute attribute) { return _eventAttributes.get(attribute); }

    public Node serialize(Document doc) {
        Element eventElem = doc.createElement("ge");

        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        return eventElem;
    }

}
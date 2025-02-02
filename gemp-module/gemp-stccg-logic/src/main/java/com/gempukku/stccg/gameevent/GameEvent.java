package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.player.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {

    public enum Type {
        PARTICIPANTS("P"), GAME_PHASE_CHANGE("GPC"), TURN_CHANGE("TC"),
        PUT_SHARED_MISSION_INTO_PLAY("PUT_SHARED_MISSION_INTO_PLAY"),
        TRIBBLE_SEQUENCE_UPDATE("TSEQ"),
        PLAYER_DECKED("PLAYER_DECKED"), // TODO: Not implemented in JavaScript
        PLAYER_SCORE("PLAYER_SCORE"),
        PUT_CARD_INTO_PLAY("PCIP"),
        PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING("PCIPAR"),
        MOVE_CARD_IN_PLAY("MCIP"),
        REMOVE_CARD_FROM_PLAY("RCFP"),
        SEND_MESSAGE("M"), SEND_WARNING("W"),
        GAME_STATS("GS"),
        CHAT_MESSAGE("CM"),
        GAME_ENDED("EG"),
        UPDATE_CARD_IMAGE("UPDATE_CARD_IMAGE"),
        CARD_AFFECTED_BY_CARD("CAC"), SHOW_CARD_ON_SCREEN("EP"), FLASH_CARD_IN_PLAY("CA"),
        DECISION("D"), GAME_STATE_CHECK("GAME_STATE_CHECK");

        private final String code;

        Type(String code) {
            this.code = code;
        }

    }

    public enum Attribute {
        /* Don't change these names without editing the client code, as it relies on the .name() method */
        allParticipantIds, blueprintId, cardId, controllerId, decisionType, discardPublic, id, imageUrl,
        locationIndex, message, otherCardIds, quadrant, participantId, phase, targetCardId, text, timestamp,
        type, zone,
        placedOnMission,
        region
    }

    private final Type _type;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private final String _typeCode;

    @JacksonXmlProperty(localName = "timeStamp", isAttribute = true)
    private final String _timeStamp;

    @JacksonXmlProperty(localName = "participantId", isAttribute = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String _playerId;

    private Zone _zone;
    protected final Map<Attribute, String> _eventAttributes = new HashMap<>();

    public GameEvent(Type type) {
        _type = type;
        _typeCode = type.code;
        _timeStamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS"));
    }


    public GameEvent(Type type, Player player) {
        this(type);
        _playerId = player.getPlayerId();
    }


    public Type getType() { return _type; }
    public Zone getZone() { return _zone; }

    public String getAttribute(Attribute attribute) { return _eventAttributes.get(attribute); }

    public Node serialize(Document doc) throws JsonProcessingException {
        Element eventElem = doc.createElement("ge");

        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        return eventElem;
    }

    public Map<Attribute, String> getAttributes() {
        return _eventAttributes;
    }


}
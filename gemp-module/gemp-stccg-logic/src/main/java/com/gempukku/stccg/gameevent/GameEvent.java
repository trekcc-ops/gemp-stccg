package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
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
        GAME_ENDED("EG"),
        UPDATE_CARD_IMAGE("UPDATE_CARD_IMAGE"),
        FLASH_CARD_IN_PLAY("CA"),
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

    DefaultGame _game;

    public GameEvent(Type type) {
        _type = type;
        _typeCode = type.code;
        _eventAttributes.put(Attribute.type, type.code);
        _eventAttributes.put(Attribute.timestamp,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS")));
        _timeStamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS"));
    }

    public GameEvent(Type type, Player player) {
        this(type);
        _playerId = player.getPlayerId();
        _eventAttributes.put(Attribute.participantId, player.getPlayerId());
    }


    void setCardData(PhysicalCard card) {
        _eventAttributes.put(Attribute.cardId, String.valueOf(card.getCardId()));
        _eventAttributes.put(Attribute.blueprintId, card.getBlueprintId());
        _eventAttributes.put(Attribute.zone, card.getZone().name());
        _zone = card.getZone();
        _eventAttributes.put(Attribute.imageUrl, card.getImageUrl());
        _eventAttributes.put(Attribute.controllerId, card.getOwnerName()); // TODO - Owner, not controller

        // int locationZoneIndex
        if (card instanceof ST1EPhysicalCard stCard) {
            GameLocation location = stCard.getGameLocation();
            if (location instanceof MissionLocation mission) {
                int locationZoneIndex = mission.getLocationZoneIndex(stCard.getGame());
                _eventAttributes.put(Attribute.locationIndex, String.valueOf(locationZoneIndex));
            } else {
                _eventAttributes.put(Attribute.locationIndex, "-1");
            }
        }

        if (card.getCardType() == CardType.MISSION && card.getGameLocation() instanceof MissionLocation mission) {
            _eventAttributes.put(Attribute.quadrant, mission.getQuadrant().name());
            if (mission.getRegion() != null)
                _eventAttributes.put(Attribute.region, mission.getRegion().name());
        }

        if (card.getStackedOn() != null)
            _eventAttributes.put(Attribute.targetCardId, String.valueOf(card.getStackedOn().getCardId()));
        else if (card.getAttachedTo() != null)
            _eventAttributes.put(Attribute.targetCardId, String.valueOf(card.getAttachedTo().getCardId()));
        if (card.isPlacedOnMission()) {
                if (card.getGameLocation() instanceof MissionLocation mission) {
                    _eventAttributes.put(Attribute.placedOnMission, "true");
                    _eventAttributes.put(Attribute.targetCardId,
                            String.valueOf(mission.getTopMissionCard().getCardId()));
                } else if (_game != null) {
                    _game.sendErrorMessage("Tried to create game event for card placed on mission," +
                            " but card is placed on a non-mission card");
                }
        }
    }

    @JsonIgnore
    public Type getType() { return _type; }

    public Zone getZone() { return _zone; }

    @JsonIgnore
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
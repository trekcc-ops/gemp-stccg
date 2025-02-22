package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.NullLocation;
import com.gempukku.stccg.player.Player;
import com.google.common.collect.Iterables;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameEvent {
    public enum Type {
        SEND_MESSAGE("M"),
        SEND_WARNING("W"),
        DECISION("DECISION"),
        ACTION_RESULT("ACTION_RESULT");

        private final String code;

        Type(String code) {
            this.code = code;
        }

    }

    public enum Attribute {
        blueprintId, cardId, controllerId, decisionType, discardPublic, id, imageUrl,
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


    void setCardData(PhysicalCard card) throws InvalidGameOperationException {
        _eventAttributes.put(Attribute.cardId, String.valueOf(card.getCardId()));
        _eventAttributes.put(Attribute.blueprintId, card.getBlueprintId());
        _zone = getZoneForCard(card);
        _eventAttributes.put(Attribute.zone, _zone.name());
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
                } else {
                    throw new InvalidGameOperationException("Tried to create game event for card placed on mission," +
                            " but card is placed on a non-mission card");
                }
        }
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

    protected Zone getZoneForCard(PhysicalCard card) throws InvalidGameOperationException {
        Set<Zone> possibleZones = new HashSet<>();
        for (Player player : card.getGame().getPlayers()) {
            if (player.getCardsInHand().contains(card))
                possibleZones.add(Zone.HAND);
            if (player.getCardsInDrawDeck().contains(card))
                possibleZones.add(Zone.DRAW_DECK);
            if (player.getDiscardPile().contains(card))
                possibleZones.add(Zone.DISCARD);
            if (player.getCardsInGroup(Zone.CORE).contains(card))
                possibleZones.add(Zone.CORE);
            if (player.getCardsInGroup(Zone.REMOVED).contains(card))
                possibleZones.add(Zone.REMOVED);
        }
        if (card.getCardType() == CardType.MISSION) {
            Zone missionZone;
            if (card.isInPlay())
                missionZone = Zone.SPACELINE;
            else if (card.getGame().getCurrentPhase() == Phase.SEED_MISSION)
                missionZone = Zone.HAND;
            else
                missionZone = Zone.MISSIONS_PILE;
            possibleZones.add(missionZone);
        }

        // TODO - 1E client doesn't use PLAY_PILE
        if (card.getAttachedTo() != null) {
            possibleZones.add(Zone.ATTACHED);
        } else if (card.getCardType() != CardType.MISSION && card.isInPlay() &&
                !(card.getGameLocation() instanceof NullLocation)) {
            possibleZones.add(Zone.AT_LOCATION);
        }

        boolean inSeedDeck = false;
        for (Player player : card.getGame().getPlayers()) {
            if (player.getCardGroupCards(Zone.SEED_DECK).contains(card)) {
                inSeedDeck = true;
                possibleZones.add(Zone.SEED_DECK);
            }
        }

        if (!inSeedDeck && !card.isInPlay() && possibleZones.isEmpty()) {
            possibleZones.add(Zone.VOID);
        }

        if (possibleZones.size() == 1) {
            return Iterables.getOnlyElement(possibleZones);
        } else {
            throw new InvalidGameOperationException("Unable to assign zone for card " + card.getTitle());
        }
    }

}
package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;

public class AddNewCardGameEvent extends GameEvent {

    private final PhysicalCard _card;

    public AddNewCardGameEvent(GameEvent.Type eventType, PhysicalCard card) throws InvalidGameOperationException {
        super(eventType, card.getOwner());
        _card = card;
        setCardData(card);
    }

    @JsonProperty("imageUrl")
    private String getImageUrl() {
        return _card.getImageUrl();
    }

    @JsonProperty("cardId")
    private String getCardId() {
        return String.valueOf(_card.getCardId());
    }

    @JsonProperty("zone")
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Zone getZone() throws InvalidGameOperationException {
        return getZoneForCard(_game, _card);
    }

    @JsonProperty("controllerId")
    public String getControllerId() {
        return _card.getController().getPlayerId();
    }

    @JsonProperty("locationIndex")
    public String getLocationIndex() {
        if (_card instanceof ST1EPhysicalCard stCard) {
            GameLocation location = stCard.getGameLocation();
            if (location instanceof MissionLocation mission) {
                int locationZoneIndex = mission.getLocationZoneIndex(stCard.getGame());
                return String.valueOf(locationZoneIndex);
            }
        }
        return "-1";
    }

    @JsonProperty("blueprintId")
    public String getBlueprintId() {
        return _card.getBlueprintId();
    }

    @JsonProperty("targetCardId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getTargetCardId() {
        if (_card.getStackedOn() != null)
            return String.valueOf(_card.getStackedOn().getCardId());
        else if (_card.getAttachedTo() != null)
            return String.valueOf(_card.getAttachedTo().getCardId());
        if (_card.isPlacedOnMission() && _card.getGameLocation() instanceof MissionLocation mission) {
            return String.valueOf(mission.getTopMissionCard().getCardId());
        }
        return null;
    }

    @JsonProperty("quadrant")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Quadrant getQuadrant() {
        if (_card.getCardType() == CardType.MISSION && _card.getGameLocation() instanceof MissionLocation mission) {
            return mission.getQuadrant();
        }
        return null;
    }

    @JsonProperty("region")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Region getRegion() {
        if (_card.getCardType() == CardType.MISSION && _card.getGameLocation() instanceof MissionLocation mission) {
            return mission.getRegion();
        }
        return null;
    }

}
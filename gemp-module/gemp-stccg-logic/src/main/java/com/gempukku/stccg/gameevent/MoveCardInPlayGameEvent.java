package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;

public class MoveCardInPlayGameEvent extends GameEvent {

    private final PhysicalCard _card;

    public MoveCardInPlayGameEvent(PhysicalCard card) {
        super(Type.MOVE_CARD_IN_PLAY, card.getOwner());
        _card = card;
    }

    @JacksonXmlProperty(localName = "cardId", isAttribute = true)
    private int getCardId() {
        return _card.getCardId();
    }

    @JacksonXmlProperty(localName = "zone", isAttribute = true)
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Zone getZone() {
        return _card.getZone();
    }

    @JacksonXmlProperty(localName = "controllerId", isAttribute = true)
    public String getControllerId() {
        return _card.getController().getPlayerId();
    }

    @JacksonXmlProperty(localName = "locationIndex", isAttribute = true)
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

    @JacksonXmlProperty(localName = "targetCardId", isAttribute = true)
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

}
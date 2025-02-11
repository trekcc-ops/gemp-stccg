package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;

public class MoveCardInPlayGameEvent extends GameEvent {

    private final PhysicalCard _card;

    public MoveCardInPlayGameEvent(PhysicalCard card) throws InvalidGameOperationException {
        super(Type.MOVE_CARD_IN_PLAY, card.getOwner());
        _card = card;
        setCardData(card);
    }

    @JsonProperty("imageUrl")
    private String getImageUrl() {
        return _card.getImageUrl();
    }

    @JsonProperty("cardId")
    private int getCardId() {
        return _card.getCardId();
    }

    @JsonProperty("zone")
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Zone getZone() throws InvalidGameOperationException {
        return getZoneForCard(_card);
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

}
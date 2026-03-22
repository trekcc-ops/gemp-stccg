package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionStatus;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public abstract class SeedCardAction extends PlayCardAction {

    @JsonCreator
    private SeedCardAction(@JsonProperty("actionId") int actionId,
                           @JsonProperty("seededCardId") @JsonIdentityReference(alwaysAsId=true)
                           PhysicalCard cardEnteringPlay,
                           @JsonProperty("performingCardId") @JsonIdentityReference(alwaysAsId=true)
                               PhysicalCard performingCard,
                           @JsonProperty("performingPlayerId")
                           String performingPlayerName,
                           @JsonProperty("destinationZone")
                           Zone destinationZone,
                           @JsonProperty("status")
                           ActionStatus status) {
        super(actionId, performingCard, cardEnteringPlay, performingPlayerName, destinationZone, ActionType.SEED_CARD,
                status);
    }

    protected SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, Zone zone) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD);
    }

    @JsonProperty("seededCardId")
    protected int getSeededCardId() {
        if (_cardEnteringPlay != null) {
            return _cardEnteringPlay.getCardId();
        } else {
            return 0;
        }
    }

}
package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class FlyShipActionResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _shipFlying;

    @JsonProperty("originLocationId")
    private final int _fromLocationId;
    @JsonProperty("destinationLocationId")
    private final int _toLocationId;


    public FlyShipActionResult(DefaultGame cardGame, FlyShipAction action, ShipCard shipFlying, int fromLocationId,
                               int toLocationId) {
        super(cardGame, ActionResultType.FLEW_SHIP, action);
        _shipFlying = shipFlying;
        _fromLocationId = fromLocationId;
        _toLocationId = toLocationId;
    }
}
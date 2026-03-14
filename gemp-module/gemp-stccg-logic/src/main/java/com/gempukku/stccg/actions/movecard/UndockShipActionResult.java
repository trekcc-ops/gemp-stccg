package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class UndockShipActionResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _shipUndocking;

    @JsonProperty("undockingFromCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _undockingFrom;

    public UndockShipActionResult(DefaultGame cardGame, UndockAction action, ShipCard shipDocking,
                                  PhysicalCard undockingFrom) {
        super(cardGame, ActionResultType.UNDOCKED, action);
        _shipUndocking = shipDocking;
        _undockingFrom = undockingFrom;
    }
}
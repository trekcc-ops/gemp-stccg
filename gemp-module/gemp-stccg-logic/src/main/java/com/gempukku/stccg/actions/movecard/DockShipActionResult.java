package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;

public class DockShipActionResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _shipDocking;

    @JsonProperty("dockedAtCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final FacilityCard _dockedAtFacility;

    public DockShipActionResult(DefaultGame cardGame, DockAction action, ShipCard shipDocking, FacilityCard destination) {
        super(cardGame, ActionResultType.DOCK_SHIP, action);
        _shipDocking = shipDocking;
        _dockedAtFacility = destination;
    }
}
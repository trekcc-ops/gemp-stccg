package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public class DiscardCardResult extends ActionResult {

    @JsonProperty("targetCardId")
    protected final PhysicalCard _discardedCard;

    @JsonProperty("destination")
    private final Zone _destination;

    public DiscardCardResult(DefaultGame cardGame, PhysicalCard card, Action action, Zone destination) {
        super(cardGame, ActionResultType.DISCARD, action);
        _discardedCard = card;
        _destination = destination;
    }


}
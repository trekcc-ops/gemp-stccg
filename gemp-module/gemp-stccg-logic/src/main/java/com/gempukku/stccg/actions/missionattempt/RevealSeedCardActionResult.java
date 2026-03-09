package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class RevealSeedCardActionResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _revealedCard;

    public RevealSeedCardActionResult(DefaultGame cardGame, RevealSeedCardAction action, PhysicalCard revealedCard) {
        super(cardGame, ActionResultType.REVEALED_SEED_CARD, action);
        _revealedCard = revealedCard;
    }

}
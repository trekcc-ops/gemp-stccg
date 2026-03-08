package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;

public class KillCardResult extends ActionResult {

    private final PhysicalCard _killedCard;

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    public KillCardResult(DefaultGame cardGame, KillAction action, PhysicalCard killedCard) {
        super(cardGame, ActionResultType.KILL, action);
        _killedCard = killedCard;
        _performingCard = action.getPerformingCard();
    }

    @JsonIgnore
    public PhysicalCard getKilledCard() { return _killedCard; }

    @JsonProperty("killedCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> getKilledCards() {
        return Collections.singleton(_killedCard);
    }
}
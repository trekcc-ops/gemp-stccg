package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class WalkCardsResult extends ActionResult {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsWalked;

    @JsonProperty("originCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _origin;

    @JsonProperty("destinationCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _destination;


    public WalkCardsResult(DefaultGame cardGame, String performingPlayerId, Action action,
                           Collection<PhysicalCard> cardsWalked, PhysicalCard origin, PhysicalCard destination) {
        super(cardGame, ActionResultType.WALK_CARDS, performingPlayerId, action);
        _cardsWalked = cardsWalked;
        _origin = origin;
        _destination = destination;
    }
}
package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class BeamCardsResult extends ActionResult {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsBeamed;

    @JsonProperty("originId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _origin;

    @JsonProperty("destinationId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _destination;

    @JsonProperty("originLocationId")
    private final int _originLocationId;


    public BeamCardsResult(DefaultGame cardGame, String performingPlayerId, Action action,
                           Collection<PhysicalCard> cardsBeamed, PhysicalCard origin, PhysicalCard destination) {
        super(cardGame, ActionResultType.BEAM_CARDS, performingPlayerId, action);
        _cardsBeamed = cardsBeamed;
        _origin = origin;
        _destination = destination;
        _originLocationId = origin.getLocationId();
    }
}
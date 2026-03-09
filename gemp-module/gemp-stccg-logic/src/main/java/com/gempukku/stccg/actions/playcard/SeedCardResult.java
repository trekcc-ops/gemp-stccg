package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.game.DefaultGame;

public class SeedCardResult extends ActionResult {

    @JsonProperty("seededCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _playedCard;

    @JsonProperty("destinationCardId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _destinationCard;

    @JsonProperty("toCore")
    private final boolean _toCore;

    public SeedCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard, PhysicalCard destinationCard) {
        super(cardGame, ActionResultType.SEEDED_INTO_PLAY, action);
        _playedCard = playedCard;
        if (destinationCard instanceof ProxyCoreCard) {
            _destinationCard = null;
            _toCore = true;
        } else {
            _destinationCard = destinationCard;
            _toCore = false;
        }
    }

    public SeedCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard) {
        this(cardGame, action, playedCard, null);
    }


    @JsonIgnore
    public Action getAction() { return _action; }

}
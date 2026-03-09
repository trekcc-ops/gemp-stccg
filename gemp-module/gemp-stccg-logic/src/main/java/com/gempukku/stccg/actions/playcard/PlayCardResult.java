package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.game.DefaultGame;

public class PlayCardResult extends ActionResult {

    @JsonProperty("playedCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _playedCard;

    @JsonProperty("destinationCardId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _destinationCard;

    @JsonProperty("toCore")
    private final boolean _toCore;
    private final ActionType _actionType;

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard) {
        super(cardGame, ActionResultType.PLAYED_CARD, action);
        _playedCard = playedCard;
        _toCore = false;
        _actionType = null;
        _destinationCard = null;
    }

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard, PhysicalCard destinationCard,
                          ActionType actionType) {
        super(cardGame, ActionResultType.PLAYED_CARD, action);
        _actionType = actionType;
        _playedCard = playedCard;
        if (destinationCard instanceof ProxyCoreCard) {
            _destinationCard = null;
            _toCore = true;
        } else {
            _destinationCard = destinationCard;
            _toCore = false;
        }
    }

    @JsonProperty("isDownload")
    private boolean isDownload() {
        return _actionType == ActionType.DOWNLOAD_CARD;
    }


    @JsonIgnore
    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    @JsonIgnore
    public Action getAction() { return _action; }

}
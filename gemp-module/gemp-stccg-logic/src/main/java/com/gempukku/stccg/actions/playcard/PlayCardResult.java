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
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
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

    @JsonProperty("isDownload")
    private final boolean _isDownload;

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;


    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard, PhysicalCard performingCard) {
        super(cardGame, ActionResultType.PLAYED_CARD, action);
        _playedCard = playedCard;
        _toCore = false;
        _isDownload = false;
        _performingCard = performingCard;
        _destinationCard = null;
    }

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard, PhysicalCard destinationCard,
                          ActionType actionType, PhysicalCard performingCard) {
        super(cardGame, ActionResultType.PLAYED_CARD, action);
        _isDownload = actionType == ActionType.DOWNLOAD_CARD;
        _performingCard = performingCard;
        _playedCard = playedCard;
        if (destinationCard instanceof ProxyCoreCard) {
            _destinationCard = null;
            _toCore = true;
        } else {
            _destinationCard = destinationCard;
            _toCore = false;
        }
    }

    @JsonIgnore
    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    @JsonProperty("isReport")
    public boolean isReport() {
        return _playedCard instanceof ReportableCard;
    }

}
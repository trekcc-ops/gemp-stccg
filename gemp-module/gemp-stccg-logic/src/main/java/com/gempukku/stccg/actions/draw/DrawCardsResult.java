package com.gempukku.stccg.actions.draw;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyAnonymousCard;
import com.gempukku.stccg.game.DefaultGame;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class DrawCardsResult extends ActionResult {

    @JsonProperty("drawnCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsDrawn = new ArrayList<>();

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final PhysicalCard _performingCard;

    @JsonProperty("isStartingHand")
    private final boolean _isStartingHand;

    public DrawCardsResult(DefaultGame cardGame, Action action, Collection<PhysicalCard> cardsDrawn,
                           boolean isStartingHand, PhysicalCard performingCard) {
        super(cardGame, ActionResultType.DREW_CARDS, action);
        _cardsDrawn.addAll(cardsDrawn);
        _isStartingHand = isStartingHand;
        _performingCard = performingCard;
    }

    public DrawCardsResult(DefaultGame cardGame, Action action, Collection<PhysicalCard> cardsDrawn,
                           boolean isStartingHand) {
        this(cardGame, action, cardsDrawn, isStartingHand, null);
    }

    private DrawCardsResult(int resultId, Action action, Collection<PhysicalCard> cardsDrawn,
                            ZonedDateTime timestamp, boolean isStartingHand, PhysicalCard performingCard) {
        super(resultId, ActionResultType.DREW_CARDS, action.getPerformingPlayerId(), action, timestamp);
        _cardsDrawn.addAll(cardsDrawn);
        _isStartingHand = isStartingHand;
        _performingCard = performingCard;
    }

    @Override
    public DrawCardsResult getResultForPlayer(String requestingPlayerName) {
        if (_performingPlayerId.equals(requestingPlayerName)) {
            return this;
        } else {
            Collection<PhysicalCard> anonymousCards = new ArrayList<>();
            for (int i = 0; i < _cardsDrawn.size(); i++) {
                anonymousCards.add(new ProxyAnonymousCard(_performingPlayerId));
            }
            return new DrawCardsResult(_resultId, _action, anonymousCards, _timestamp, _isStartingHand, _performingCard);
        }
    }

}
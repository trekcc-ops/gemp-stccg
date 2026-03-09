package com.gempukku.stccg.actions.draw;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
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

    public DrawCardsResult(DefaultGame cardGame, Action action, PhysicalCard cardDrawn) {
        super(cardGame, ActionResultType.DREW_CARDS, action);
        _cardsDrawn.add(cardDrawn);
    }

    public DrawCardsResult(DefaultGame cardGame, Action action, Collection<PhysicalCard> cardsDrawn) {
        super(cardGame, ActionResultType.DREW_CARDS, action);
        _cardsDrawn.addAll(cardsDrawn);
    }

    private DrawCardsResult(int resultId, Action action, Collection<PhysicalCard> cardsDrawn,
                            ZonedDateTime timestamp) {
        super(resultId, ActionResultType.DREW_CARDS, action.getPerformingPlayerId(), action, timestamp);
        _cardsDrawn.addAll(cardsDrawn);
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
            return new DrawCardsResult(_resultId, _action, anonymousCards, _timestamp);
        }
    }

}
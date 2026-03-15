package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ChildCardRelationshipType;

public class UndockAction extends ActionyAction implements CardPerformedAction {
    private final ShipCard _performingCard;

    public UndockAction(DefaultGame cardGame, String performingPlayerName, ShipCard cardUndocking) {
        super(cardGame, performingPlayerName, ActionType.UNDOCK_SHIP);
        _performingCard = cardUndocking;
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public void processEffect(DefaultGame cardGame) {
        PhysicalCard dockedAtCard = _performingCard.getDockedAtCard();
        if (dockedAtCard != null && dockedAtCard.getCardType() == CardType.FACILITY) {
            _performingCard.setParentCardRelationship(dockedAtCard.getParentCard(), ChildCardRelationshipType.IN_SPACE);
        } else if (dockedAtCard != null && dockedAtCard.getCardType() == CardType.SITE) {
            _performingCard.setParentCardRelationship(dockedAtCard.getParentCard().getParentCard(), ChildCardRelationshipType.IN_SPACE);
        }
        saveResult(new UndockShipActionResult(cardGame, this, _performingCard, dockedAtCard), cardGame);
        setAsSuccessful();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _performingCard.isDocked(); }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    public ShipCard getCardToMove() { return _performingCard; }

}
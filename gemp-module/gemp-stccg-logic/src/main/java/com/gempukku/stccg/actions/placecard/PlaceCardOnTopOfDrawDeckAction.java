package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;

public class PlaceCardOnTopOfDrawDeckAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final FixedCardResolver _cardTarget;

    public PlaceCardOnTopOfDrawDeckAction(Player performingPlayer, PhysicalCard cardBeingPlaced) {
        super(cardBeingPlaced.getGame(), performingPlayer, ActionType.PLACE_CARD_ON_TOP_OF_DRAW_DECK);
        _cardTarget = new FixedCardResolver(cardBeingPlaced);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        PhysicalCard cardBeingPlaced = _cardTarget.getCard();
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(cardBeingPlaced));
        cardGame.sendMessage(_performingPlayerId + " placed " + cardBeingPlaced + " on top of their draw deck");

        CardPile drawDeck = cardBeingPlaced.getOwner().getDrawDeck();
        drawDeck.addCardToTop(cardBeingPlaced);
        cardBeingPlaced.setZone(Zone.DRAW_DECK);

        setAsSuccessful();
        return getNextAction();
    }

    @JsonProperty("targetCardId")
    private PhysicalCard getTargetCard() {
        return _cardTarget.getCard();
    }
}
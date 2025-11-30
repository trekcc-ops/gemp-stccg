package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlaceCardOnTopOfDrawDeckAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final FixedCardResolver _cardTarget;

    public PlaceCardOnTopOfDrawDeckAction(String performingPlayerName, PhysicalCard cardBeingPlaced) {
        super(cardBeingPlaced.getGame(), performingPlayerName, ActionType.PLACE_CARD_ON_TOP_OF_DRAW_DECK);
        _cardTarget = new FixedCardResolver(cardBeingPlaced);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        PhysicalCard cardBeingPlaced = _cardTarget.getCard();
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(cardBeingPlaced));

        Player cardOwner = cardGame.getPlayer(cardBeingPlaced.getOwnerName());
        DrawDeck drawDeck = cardOwner.getDrawDeck();
        drawDeck.addCardToTop(cardBeingPlaced);

        setAsSuccessful();
        return getNextAction();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardId")
    private PhysicalCard getTargetCard() {
        return _cardTarget.getCard();
    }
}
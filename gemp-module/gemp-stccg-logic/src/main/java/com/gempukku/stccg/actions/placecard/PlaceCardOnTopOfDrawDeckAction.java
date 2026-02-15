package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.FixedCardResolver;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlaceCardOnTopOfDrawDeckAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final FixedCardResolver _cardTarget;

    public PlaceCardOnTopOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardBeingPlaced) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARD_ON_TOP_OF_DRAW_DECK);
        _cardTarget = new FixedCardResolver(cardBeingPlaced);
        _cardTargets.add(_cardTarget);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        try {
            PhysicalCard cardBeingPlaced = getTargetCard();
            GameState gameState = cardGame.getGameState();
            gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(cardBeingPlaced));

            Player cardOwner = cardGame.getPlayer(cardBeingPlaced.getOwnerName());
            DrawDeck drawDeck = cardOwner.getDrawDeck();
            drawDeck.addCardToTop(cardBeingPlaced);

            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardId")
    private PhysicalCard getTargetCard() {
        return _cardTarget.getCard();
    }
}
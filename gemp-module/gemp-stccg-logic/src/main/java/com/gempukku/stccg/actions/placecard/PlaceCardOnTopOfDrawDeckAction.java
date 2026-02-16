package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class PlaceCardOnTopOfDrawDeckAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    public PlaceCardOnTopOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName, ActionCardResolver cardTarget) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARD_ON_TOP_OF_DRAW_DECK);
        _cardTarget = cardTarget;
        _cardTargets.add(_cardTarget);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        try {
            Collection<PhysicalCard> cardBeingPlaced = getTargetCards();
            GameState gameState = cardGame.getGameState();
            gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, cardBeingPlaced);

            for (PhysicalCard card : cardBeingPlaced) {
                Player cardOwner = cardGame.getPlayer(card.getOwnerName());
                DrawDeck drawDeck = cardOwner.getDrawDeck();
                drawDeck.addCardToTop(card);
            }

            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardIds")
    private Collection<PhysicalCard> getTargetCards() {
        return _cardTarget.getCards();
    }
}
package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class PlaceCardsOnBottomOfDrawDeckAction extends ActionyAction {

    private SelectCardsAction _selectionAction;
    private Collection<PhysicalCard> _cardsToPlace;
    private enum Progress { cardsSelected }


    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, Player performingPlayer,
                                              SelectCardsAction selectionAction) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARDS_BENEATH_DRAW_DECK, Progress.values());
        _selectionAction = selectionAction;
    }

    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName,
                                              Collection<PhysicalCard> cardsToPlace) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARDS_BENEATH_DRAW_DECK);
        _cardsToPlace = cardsToPlace;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!getProgress(Progress.cardsSelected)) {
            if (_selectionAction != null && !_selectionAction.wasCarriedOut()) {
                return _selectionAction;
            } else if (_selectionAction != null) {
                _cardsToPlace = _selectionAction.getSelectedCards();
                setProgress(Progress.cardsSelected);
            } else {
                throw new InvalidGameLogicException("Unable to identify cards to place on bottom of deck");
            }
        }

        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, _cardsToPlace);

        for (PhysicalCard card : _cardsToPlace) {
            Player cardOwner = cardGame.getPlayer(card.getOwnerName());
            DrawDeck drawDeck = cardOwner.getDrawDeck();
            drawDeck.addCardToBottom(card);
            card.setZone(Zone.DRAW_DECK);
            setAsSuccessful();
        }
        return getNextAction();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardIds")
    private Collection<PhysicalCard> getTargetCards() {
        return _cardsToPlace;
    }
}
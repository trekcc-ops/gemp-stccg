package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlaceCardOnBottomOfPlayPileAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    public PlaceCardOnBottomOfPlayPileAction(DefaultGame cardGame, Player performingPlayer,
                                             SelectCardsAction selectCardAction) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD_IN_PLAY_PILE);
        _cardTarget = new SelectCardsResolver(selectCardAction);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        if (!_wasCarriedOut) {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            for (PhysicalCard card : _cardTarget.getCards(cardGame)) {
                cardGame.getGameState().removeCardsFromZone(cardGame, performingPlayer, List.of(card));
                cardGame.sendMessage(_performingPlayerId + " puts " + card.getCardLink() +
                        " from hand on bottom of their play pile");
                cardGame.getGameState().addCardToBottomOfPlayPile(card);
                _wasCarriedOut = true;
                setAsSuccessful();
            }
        }

        return getNextAction();
    }
}
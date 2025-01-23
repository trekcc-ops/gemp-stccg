package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.SelectCardsResolver;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class PlaceCardOnBottomOfPlayPileAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    public PlaceCardOnBottomOfPlayPileAction(DefaultGame cardGame, Player performingPlayer,
                                             SelectCardsAction selectCardAction) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD);
        _cardTarget = new SelectCardsResolver(selectCardAction);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        if (!_wasCarriedOut) {
            for (PhysicalCard card : _cardTarget.getCards(cardGame)) {
                cardGame.removeCardsFromZone(_performingPlayerId, List.of(card));
                cardGame.sendMessage(_performingPlayerId + " puts " + card.getCardLink() +
                        " from hand on bottom of their play pile");
                cardGame.getGameState().addCardToZone(card, Zone.PLAY_PILE, false);
                _wasCarriedOut = true;
            }
        }

        return getNextAction();
    }
}
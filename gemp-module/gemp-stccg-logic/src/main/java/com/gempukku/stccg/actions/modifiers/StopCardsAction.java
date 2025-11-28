package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;

public class StopCardsAction extends ActionyAction {
    private final ActionCardResolver _cardTarget;

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> _stoppedCards;

    public StopCardsAction(DefaultGame cardGame, Player performingPlayer,
                           Collection<? extends ST1EPhysicalCard> cardsToStop) {
        super(cardGame, performingPlayer, "Stop cards", ActionType.STOP_CARDS);
        _cardTarget = new FixedCardsResolver(cardsToStop);
    }

    public StopCardsAction(DefaultGame cardGame, String performingPlayerName,
                           Collection<? extends ST1EPhysicalCard> cardsToStop) {
        super(cardGame, performingPlayerName, "Stop cards", ActionType.STOP_CARDS);
        _cardTarget = new FixedCardsResolver(cardsToStop);
    }

    public StopCardsAction(DefaultGame cardGame, String performingPlayerName, SelectCardsAction selectionAction) {
        super(cardGame, performingPlayerName, "Stop cards", ActionType.STOP_CARDS);
        _cardTarget = new SelectCardsResolver(selectionAction);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null) {
                if (selectionAction.wasCarriedOut()) {
                    _cardTarget.resolve(cardGame);
                } else {
                    return selectionAction;
                }
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        if (!wasCompleted()) {
            _stoppedCards = _cardTarget.getCards(cardGame);
            Collection<ST1EPhysicalCard> cardsToStop = new LinkedList<>();
            for (PhysicalCard card : _cardTarget.getCards(cardGame)) {
                if (card instanceof ST1EPhysicalCard stCard) {
                    cardsToStop.add(stCard);
                } else {
                    setAsFailed();
                    throw new InvalidGameLogicException(
                            "Tried to \"stop\" a card from a game with no \"stop\" action");
                }
            }
            for (ST1EPhysicalCard card : cardsToStop) {
                card.stop();
            }
            setAsSuccessful();
        }

        return getNextAction();
    }

}
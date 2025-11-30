package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class DiscardSingleCardAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private Collection<PhysicalCard> _cardsDiscarded; // may not be initialized

    public DiscardSingleCardAction(DefaultGame cardGame, PhysicalCard performingCard, Player performingPlayer,
                                   SelectVisibleCardAction selectAction) {
        super(cardGame, performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardTarget = new SelectCardsResolver(selectAction);
    }


    public DiscardSingleCardAction(PhysicalCard performingCard, Player performingPlayer, PhysicalCard cardToDiscard) {
        super(performingCard.getGame(), performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new FixedCardResolver(cardToDiscard);
        _performingCard = performingCard;
    }

    public DiscardSingleCardAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName, PhysicalCard cardToDiscard) {
        super(cardGame, performingPlayerName, "Discard", ActionType.DISCARD);
        _cardTarget = new FixedCardResolver(cardToDiscard);
        _performingCard = performingCard;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
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

        _cardsDiscarded = _cardTarget.getCards(cardGame);
        if (_cardsDiscarded.size() != 1)
            throw new InvalidGameLogicException("Discarding too many cards for DiscardSingleCardAction");
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, _cardsDiscarded);
        for (PhysicalCard cardToDiscard : _cardsDiscarded) {
            if (cardToDiscard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
            gameState.addCardToZoneWithoutSendingToClient(cardToDiscard, Zone.DISCARD);
            saveResult(new DiscardCardFromPlayResult(cardToDiscard, this));
        }
        setAsSuccessful();
        return getNextAction();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard cardsDiscarded() {
        if (_cardsDiscarded != null && _cardsDiscarded.size() == 1) {
            return Iterables.getOnlyElement(_cardsDiscarded);
        } else {
            return null;
        }
    }

}
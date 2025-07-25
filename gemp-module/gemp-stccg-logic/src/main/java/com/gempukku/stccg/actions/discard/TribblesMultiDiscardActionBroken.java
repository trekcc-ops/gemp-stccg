package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class TribblesMultiDiscardActionBroken extends ActionyAction implements TopLevelSelectableAction {

    /* TODO - There is a bug in this class, because each individual discard action is not created as a
        separate action. */

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private Collection<PhysicalCard> _cardsDiscarded; // may not be initialized

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard, Player performingPlayer,
                                            SelectVisibleCardAction selectAction) {
        super(cardGame, performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardTarget = new SelectCardsResolver(selectAction);
    }


    public TribblesMultiDiscardActionBroken(PhysicalCard performingCard, Player performingPlayer,
                                            Collection<PhysicalCard> cardsToDiscard) {
        super(performingCard.getGame(), performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new FixedCardsResolver(cardsToDiscard);
        _performingCard = performingCard;
    }

    public TribblesMultiDiscardActionBroken(PhysicalCard performingCard, Player performingPlayer, CardFilter cardFilter) {
        super(performingCard.getGame(), performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new CardFilterResolver(cardFilter);
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

        Collection<PhysicalCard> cardsToDiscard = _cardTarget.getCards(cardGame);
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, cardsToDiscard);
        for (PhysicalCard cardToDiscard : cardsToDiscard) {
            if (cardToDiscard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
            gameState.addCardToZoneWithoutSendingToClient(cardToDiscard, Zone.DISCARD);
            saveResult(new DiscardCardFromPlayResult(cardToDiscard, this));
        }
        setAsSuccessful();
        return getNextAction();
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> cardsDiscarded() {
        return _cardsDiscarded;
    }

}
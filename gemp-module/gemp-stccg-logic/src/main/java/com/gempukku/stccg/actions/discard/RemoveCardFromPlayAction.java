package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoveCardFromPlayAction extends ActionyAction {

    private final FixedCardResolver _cardTarget;

    public RemoveCardFromPlayAction(DefaultGame cardGame, Player performingPlayer, PhysicalCard cardToRemove) {
        super(cardGame, performingPlayer, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
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

        Collection<PhysicalCard> removedCards = List.of(_cardTarget.getCard());

        Set<PhysicalCard> toRemoveFromZone = new HashSet<>(removedCards);

        cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, toRemoveFromZone);
        for (PhysicalCard removedCard : removedCards) {
            cardGame.getGameState().addCardToZoneWithoutSendingToClient(removedCard, Zone.REMOVED);
            if (removedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }

        setAsSuccessful();

        return getNextAction();
    }

    @JsonProperty("targetCardId")
    private int targetCardId() {
        return _cardTarget.getCard().getCardId();
    }
}
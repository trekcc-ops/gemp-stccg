package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RemoveCardFromPlayAction extends ActionyAction {

    private final ActionCardResolver _cardTarget;

    public RemoveCardFromPlayAction(Player performingPlayer, PhysicalCard cardToRemove) {
        super(cardToRemove.getGame(), performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardTarget = new FixedCardResolver(cardToRemove);
    }

    public RemoveCardFromPlayAction(DefaultGame cardGame, Player performingPlayer,
                                    Collection<PhysicalCard> cardsToRemove) {
        super(cardGame, performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardTarget = new FixedCardsResolver(cardsToRemove);
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

        Collection<PhysicalCard> removedCards = new HashSet<>(_cardTarget.getCards(cardGame));

        Set<PhysicalCard> toRemoveFromZone = new HashSet<>(removedCards);

        cardGame.getGameState().removeCardsFromZone(_performingPlayerId, toRemoveFromZone);
        for (PhysicalCard removedCard : removedCards) {
            cardGame.getGameState().addCardToZone(removedCard, Zone.REMOVED);
            if (removedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }

        cardGame.sendMessage(_performingPlayerId + " removed " + TextUtils.getConcatenatedCardLinks(removedCards) +
                " from the game");

        return getNextAction();
    }
}
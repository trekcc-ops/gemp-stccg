package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
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
    private final Collection<PhysicalCard> _cardsToRemove;

    public RemoveCardFromPlayAction(Player performingPlayer, PhysicalCard cardToRemove) {
        super(performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardsToRemove = Collections.singleton(cardToRemove);
    }

    public RemoveCardFromPlayAction(Player performingPlayer, Collection<PhysicalCard> cardsToRemove) {
        super(performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardsToRemove = cardsToRemove;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Collection<PhysicalCard> removedCards = new HashSet<>(_cardsToRemove);

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
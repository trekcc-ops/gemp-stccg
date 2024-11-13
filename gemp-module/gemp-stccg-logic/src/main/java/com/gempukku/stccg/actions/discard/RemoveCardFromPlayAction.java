package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
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
    private final PhysicalCard _cardCausingRemoval;

    public RemoveCardFromPlayAction(Player performingPlayer, PhysicalCard cardRemoving, PhysicalCard cardToRemove) {
        super(performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardsToRemove = Collections.singleton(cardToRemove);
        _cardCausingRemoval = cardRemoving;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardCausingRemoval;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _cardCausingRemoval;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Set<PhysicalCard> removedCards = new HashSet<>();
        for (PhysicalCard physicalCard : _cardsToRemove)
            removedCards.add(physicalCard);

        Set<PhysicalCard> toRemoveFromZone = new HashSet<>();
        toRemoveFromZone.addAll(removedCards);

        cardGame.getGameState().removeCardsFromZone(_performingPlayerId, toRemoveFromZone);
        for (PhysicalCard removedCard : removedCards)
            cardGame.getGameState().addCardToZone(removedCard, Zone.REMOVED);

        cardGame.sendMessage(_performingPlayerId + " removed " + TextUtils.getConcatenatedCardLinks(removedCards) +
                " from the game");

        return getNextAction();
    }
}
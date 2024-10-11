package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.discard.DiscardUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RemoveCardsFromTheGameEffect extends DefaultEffect {
    private final String _playerPerforming;
    private final PhysicalCard _source;
    private final Collection<PhysicalCard> _cardsToRemove;

    public RemoveCardsFromTheGameEffect(DefaultGame game, String playerPerforming, PhysicalCard source,
                                        Collection<PhysicalCard> cardsToRemove) {
        super(game, playerPerforming);
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
    }


    @Override
    public boolean isPlayableInFull() {
        for (PhysicalCard physicalCard : _cardsToRemove) {
            if (!physicalCard.getZone().isInPlay())
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        Set<PhysicalCard> removedCards = new HashSet<>();
        for (PhysicalCard physicalCard : _cardsToRemove)
            if (physicalCard.getZone().isInPlay())
                removedCards.add(physicalCard);

        Set<PhysicalCard> discardedCards = new HashSet<>();

        Set<PhysicalCard> toMoveFromZoneToDiscard = new HashSet<>();

        DiscardUtils.cardsToChangeZones(_game, removedCards, discardedCards, toMoveFromZoneToDiscard);

        Set<PhysicalCard> toRemoveFromZone = new HashSet<>();
        toRemoveFromZone.addAll(removedCards);
        toRemoveFromZone.addAll(toMoveFromZoneToDiscard);

        _game.getGameState().removeCardsFromZone(_playerPerforming, toRemoveFromZone);
        for (PhysicalCard removedCard : removedCards)
            _game.getGameState().addCardToZone(removedCard, Zone.REMOVED);
        for (PhysicalCard card : toMoveFromZoneToDiscard)
            _game.getGameState().addCardToZone(card, Zone.DISCARD);

        _game.sendMessage(_playerPerforming + " removed " + TextUtils.getConcatenatedCardLinks(removedCards) + " from the game using " + _source.getCardLink());

        return new FullEffectResult(_cardsToRemove.size() == removedCards.size());
    }
}
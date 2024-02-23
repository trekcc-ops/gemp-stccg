package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.utils.DiscardUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RemoveCardsFromTheGameEffect extends DefaultEffect {
    private final String _playerPerforming;
    private final PhysicalCard _source;
    private final Collection<? extends PhysicalCard> _cardsToRemove;
    private final DefaultGame _game;

    public RemoveCardsFromTheGameEffect(DefaultGame game, String playerPerforming, PhysicalCard source,
                                        Collection<? extends PhysicalCard> cardsToRemove) {
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
        _game = game;
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

        _game.getGameState().sendMessage(_playerPerforming + " removed " + GameUtils.getConcatenatedCardLinks(removedCards) + " from the game using " + _source.getCardLink());

        return new FullEffectResult(_cardsToRemove.size() == removedCards.size());
    }
}

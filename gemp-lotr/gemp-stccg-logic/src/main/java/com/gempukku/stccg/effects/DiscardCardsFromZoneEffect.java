package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DiscardCardFromHandResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DiscardCardsFromZoneEffect extends AbstractEffect<DefaultGame> {
    private final String _playerId;
    private final Collection<PhysicalCard> _cards;
    private final boolean _forced;
    private final PhysicalCard _source;
    private final Zone _fromZone;

    public DiscardCardsFromZoneEffect(PhysicalCard source, Zone fromZone, PhysicalCard cardToDiscard) {
        this(source, fromZone, source.getOwner(), Collections.singleton(cardToDiscard), false);
    }
    public DiscardCardsFromZoneEffect(PhysicalCard source, Zone fromZone, Collection<PhysicalCard> cardsToDiscard) {
        this(source, fromZone, source.getOwner(), cardsToDiscard, false);
    }

    public DiscardCardsFromZoneEffect(PhysicalCard source, Zone fromZone, String playerId,
                                      Collection<PhysicalCard> cards, boolean forced) {
        _source = source;
        _playerId = playerId;
        _cards = cards;
        _forced = forced;
        _fromZone = fromZone;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Discard from " + _fromZone.getHumanReadable() + " - " + getAppendedTextNames(_cards);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        boolean playable = true;
        for (PhysicalCard card: _cards) {
            if (card.getZone() != _fromZone) {
                playable = false; // If discarding from draw deck or hand
                break;
            }
        }

        if (_forced && !game.getModifiersQuerying().canDiscardCardsFromHand(game, _playerId, _source) && _fromZone == Zone.HAND)
            playable = false;
        return playable;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            Set<PhysicalCard> discardedCards = new HashSet<>(_cards);

            gameState.removeCardsFromZone(_playerId, discardedCards);
            for (PhysicalCard card : discardedCards) {
                gameState.addCardToZone(game, card, Zone.DISCARD);
                if (_fromZone == Zone.HAND) {
                    game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromHandResult(_source, card, _forced)
                    );
                }
            }

            if (discardedCards.size() > 0)
                gameState.sendMessage(_playerId + " discarded " + getAppendedNames(discardedCards) + " from " +
                        _fromZone.getHumanReadable());

            return new FullEffectResult(discardedCards.size() == _cards.size());
        }
        return new FullEffectResult(false);
    }
}
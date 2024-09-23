package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DiscardCardsFromZoneEffect extends DefaultEffect {
    private final String _playerId;
    private final Collection<PhysicalCard> _cards;
    private final boolean _forced;
    private final PhysicalCard _source;
    private final Zone _fromZone;
    private final DefaultGame _game;

    public DiscardCardsFromZoneEffect(DefaultGame game, PhysicalCard source, Zone fromZone,
                                      PhysicalCard cardToDiscard) {
        this(game, source, fromZone, source.getOwnerName(), Collections.singleton(cardToDiscard), false);
    }
    public DiscardCardsFromZoneEffect(ActionContext actionContext, Zone fromZone,
                                      Collection<PhysicalCard> cardsToDiscard) {
        this(actionContext.getGame(), actionContext.getSource(), fromZone, actionContext.getSource().getOwnerName(),
                cardsToDiscard, false);
    }

    public DiscardCardsFromZoneEffect(DefaultGame game, PhysicalCard source, Zone fromZone,
                                      Collection<PhysicalCard> cardsToDiscard) {
        this(game, source, fromZone, source.getOwnerName(), cardsToDiscard, false);
    }

    public DiscardCardsFromZoneEffect(ActionContext actionContext, Zone fromZone, String playerId,
                                      Collection<PhysicalCard> cards, boolean forced) {
        this(actionContext.getGame(), actionContext.getSource(), fromZone, playerId, cards, forced);
    }

    public DiscardCardsFromZoneEffect(DefaultGame game, PhysicalCard source, Zone fromZone, String playerId,
                                      Collection<PhysicalCard> cards, boolean forced) {
        super(playerId);
        _source = source;
        _playerId = playerId;
        _cards = cards;
        _forced = forced;
        _fromZone = fromZone;
        _game = game;
    }

    @Override
    public String getText() {
        return "Discard from " + _fromZone.getHumanReadable() + " - " +
                TextUtils.concatenateStrings(_cards.stream().map(PhysicalCard::getFullName));
    }

    @Override
    public boolean isPlayableInFull() {
        boolean playable = true;
        for (PhysicalCard card: _cards) {
            if (card.getZone() != _fromZone) {
                playable = false; // If discarding from draw deck or hand
                break;
            }
        }

        if (_forced && !_game.getModifiersQuerying().canDiscardCardsFromHand(_playerId, _source) &&
                _fromZone == Zone.HAND)
            playable = false;
        return playable;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            Set<PhysicalCard> discardedCards = new HashSet<>(_cards);

            gameState.removeCardsFromZone(_playerId, discardedCards);
            for (PhysicalCard card : discardedCards) {
                gameState.addCardToZone(card, Zone.DISCARD);
                if (_fromZone == Zone.HAND) {
                    _game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromHandResult(_source, card, _forced)
                    );
                }
            }

            if (!discardedCards.isEmpty())
                gameState.sendMessage(_playerId + " discarded " + TextUtils.getConcatenatedCardLinks(discardedCards) + " from " +
                        _fromZone.getHumanReadable());

            return new FullEffectResult(discardedCards.size() == _cards.size());
        }
        return new FullEffectResult(false);
    }
}
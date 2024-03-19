package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayPileResult;
import com.gempukku.stccg.rules.TextUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardCardsFromEndOfCardPileEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;
    private final boolean _forced;
    private final Zone _fromZone;
    private final EndOfPile _endOfPile;
    private final DefaultGame _game;

    public DiscardCardsFromEndOfCardPileEffect(DefaultGame game, PhysicalCard source, Zone fromZone, EndOfPile endOfPile, String playerId) {
        this(game, source, fromZone, endOfPile, playerId, 1, true);
    }

    public DiscardCardsFromEndOfCardPileEffect(DefaultGame game, PhysicalCard source, Zone fromZone, EndOfPile endOfPile,
                                               String playerId, int count, boolean forced) {
        super(playerId);
        _source = source;
        _fromZone = fromZone;
        _playerId = playerId;
        _count = count;
        _forced = forced;
        _endOfPile = endOfPile;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        if (_fromZone == Zone.DRAW_DECK) {
            if (_forced && !_game.getModifiersQuerying().canDiscardCardsFromTopOfDeck(_playerId, _source))
                return false;
        }
        return _game.getGameState().getZoneCards(_playerId, _fromZone).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            List<PhysicalCard> cardsDiscarded = new LinkedList<>();
            for (int i = 0; i < _count; i++) {
                PhysicalCard card = gameState.removeCardFromEndOfPile(_playerId, _fromZone, _endOfPile);
                if (card != null) {
                    cardsDiscarded.add(card);
                    gameState.addCardToZone(card, Zone.DISCARD);
                }
            }
            if (!cardsDiscarded.isEmpty()) {
                gameState.sendMessage(_playerId + " discards " + _endOfPile.name().toLowerCase() +
                        " cards from their " + _fromZone.getHumanReadable() + " - " + TextUtils.getConcatenatedCardLinks(cardsDiscarded));
                cardsDiscardedCallback(cardsDiscarded);
            }

            for (PhysicalCard discardedCard : cardsDiscarded) {
                if (_fromZone == Zone.DRAW_DECK)
                    _game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromDeckResult(_source, discardedCard, _forced)
                    );
                else if (_fromZone == Zone.PLAY_PILE)
                    _game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromPlayPileResult(this, _source, discardedCard)
                    );
            }
            return new FullEffectResult(_count == cardsDiscarded.size());
        }
        return new FullEffectResult(false);
    }

    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {

    }
}
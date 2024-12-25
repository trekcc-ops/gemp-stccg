package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

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
    private final String _memoryId;
    private final ActionContext _context;

    public DiscardCardsFromEndOfCardPileEffect(Zone fromZone, EndOfPile endOfPile,
                                               String playerId, ActionContext context) {
        this(fromZone, endOfPile, playerId, 1, true, context, null);
    }

    public DiscardCardsFromEndOfCardPileEffect(Zone fromZone, EndOfPile endOfPile, Player performingPlayer,
                                               PhysicalCard performingCard) {
        super(performingPlayer.getGame(), performingPlayer.getPlayerId());
        _source = performingCard;
        _fromZone = fromZone;
        _playerId = performingPlayer.getPlayerId();
        _count = 1;
        _forced = true;
        _endOfPile = endOfPile;
        _context = new DefaultActionContext(performingPlayer.getPlayerId(), _game, _source, null, null);
        _memoryId = null;
    }

    public DiscardCardsFromEndOfCardPileEffect(Zone fromZone,
                                               EndOfPile endOfPile, String playerId, int count, boolean forced,
                                               ActionContext context, String memoryId) {
        super(context.getGame(), playerId);
        _source = context.getSource();
        _fromZone = fromZone;
        _playerId = playerId;
        _count = count;
        _forced = forced;
        _endOfPile = endOfPile;
        _context = context;
        _memoryId = memoryId;
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
                _context.setCardMemory(_memoryId, cardsDiscarded);
                cardsDiscardedCallback(cardsDiscarded);
            }

            for (PhysicalCard discardedCard : cardsDiscarded) {
                if (_fromZone == Zone.DRAW_DECK)
                    _game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromDeckResult(_source, discardedCard)
                    );
                else if (_fromZone == Zone.PLAY_PILE)
                    _game.getActionsEnvironment().emitEffectResult(
                            new DiscardCardFromPlayPileResult(this, _source)
                    );
            }
            return new FullEffectResult(_count == cardsDiscarded.size());
        }
        return new FullEffectResult(false);
    }

    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {

    }
}
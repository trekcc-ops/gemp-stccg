package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.results.DiscardCardFromPlayPileResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardTopCardFromPlayPileEffect extends AbstractEffect<TribblesGame> {
    private final LotroPhysicalCard _source;
    private final String _playerId;
    private final int _count;

    public DiscardTopCardFromPlayPileEffect(LotroPhysicalCard source, String playerId) {
        this(source, playerId, 1);
    }

    public DiscardTopCardFromPlayPileEffect(LotroPhysicalCard source, String playerId, int count) {
        _source = source;
        _playerId = playerId;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return game.getGameState().getZoneCards(_playerId, Zone.PLAY_PILE).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        GameState gameState = game.getGameState();
        List<LotroPhysicalCard> cardsDiscarded = new LinkedList<>();
        for (int i = 0; i < _count; i++) {
            LotroPhysicalCard card = gameState.removeTopCardFromZone(_playerId, Zone.PLAY_PILE);
            if (card != null) {
                cardsDiscarded.add(card);
                gameState.addCardToZone(game, card, Zone.DISCARD);
            }
        }
        if (cardsDiscarded.size() > 0) {
            gameState.sendMessage(_playerId + " discards top cards from their play pile - " + getAppendedNames(cardsDiscarded));
            cardsDiscardedCallback(cardsDiscarded);
        }

        for (LotroPhysicalCard discardedCard : cardsDiscarded)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardFromPlayPileResult(_source, discardedCard));

        return new FullEffectResult(_count == cardsDiscarded.size());
    }

    protected void cardsDiscardedCallback(Collection<LotroPhysicalCard> cards) {

    }
}

package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public class StackTopCardsFromDeckEffect extends DefaultEffect {
    private final String _playerId;
    private final int _count;
    private final PhysicalCard _target;

    public StackTopCardsFromDeckEffect(DefaultGame game, String playerId, int count, PhysicalCard target) {
        super(game, playerId);
        _playerId = playerId;
        _count = count;
        _target = target;
    }

    @Override
    public boolean isPlayableInFull() {
        return _target.getZone().isInPlay() && _game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_target.getZone().isInPlay()) {
            int stacked = 0;
            for (int i = 0; i < _count; i++) {
                final PhysicalCard card = _game.getGameState().removeCardFromEndOfPile(_playerId, Zone.DRAW_DECK, EndOfPile.TOP);
                if (card != null) {
                    _game.getGameState().stackCard(card, _target);
                    stacked++;
                }
            }
            return new FullEffectResult(stacked == _count);
        }
        return new FullEffectResult(false);
    }
}

package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class StackTopCardsFromDeckEffect extends AbstractEffect {
    private final String _playerId;
    private final int _count;
    private final LotroPhysicalCard _target;

    public StackTopCardsFromDeckEffect(String playerId, int count, LotroPhysicalCard target) {
        _playerId = playerId;
        _count = count;
        _target = target;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _target.getZone().isInPlay() && game.getGameState().getDeck(_playerId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_target.getZone().isInPlay()) {
            int stacked = 0;
            for (int i = 0; i < _count; i++) {
                final LotroPhysicalCard card = game.getGameState().removeTopDeckCard(_playerId);
                if (card != null) {
                    game.getGameState().stackCard(game, card, _target);
                    stacked++;
                }
            }
            return new FullEffectResult(stacked == _count);
        }
        return new FullEffectResult(false);
    }
}

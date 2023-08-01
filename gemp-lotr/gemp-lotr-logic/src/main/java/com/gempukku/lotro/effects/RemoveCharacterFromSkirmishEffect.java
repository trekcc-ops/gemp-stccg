package com.gempukku.lotro.effects;

import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class RemoveCharacterFromSkirmishEffect extends AbstractEffect {
    private final LotroPhysicalCard _source;
    private final LotroPhysicalCard _toRemove;

    public RemoveCharacterFromSkirmishEffect(LotroPhysicalCard source, LotroPhysicalCard toRemove) {
        _source = source;
        _toRemove = toRemove;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.inSkirmish.accepts(game, _toRemove);
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().removeFromSkirmish(_toRemove);
        }
        return new FullEffectResult(false);
    }
}
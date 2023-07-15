package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class ReplaceInSkirmishEffect extends AbstractEffect {
    private final PhysicalCard _replacedBy;
    private final Filterable[] _replacingFilter;

    public ReplaceInSkirmishEffect(PhysicalCard replacedBy, Filterable... replacingFilter) {
        _replacedBy = replacedBy;
        _replacingFilter = replacingFilter;
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.countActive(game, Filters.and(_replacingFilter, Filters.inSkirmish))>0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().replaceInSkirmish(_replacedBy);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
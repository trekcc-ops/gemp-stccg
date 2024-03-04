package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;

public class CardPlayedInCurrentTurnCondition implements Condition {
    private final Filter filter;
    private final DefaultGame _game;

    public CardPlayedInCurrentTurnCondition(DefaultGame game, Filterable... filters) {
        _game = game;
        filter = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled() {
        for (EffectResult effectResult : _game.getActionsEnvironment().getTurnEffectResults()) {
            if (effectResult instanceof PlayCardResult playResult) {
                if (filter.accepts(_game, playResult.getPlayedCard()))
                    return true;
            }
        }

        return false;

    }
}

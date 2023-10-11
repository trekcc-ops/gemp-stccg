package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.results.PlayCardResult;

public class CardPlayedInCurrentTurnCondition implements Condition {
    private final Filter filter;

    public CardPlayedInCurrentTurnCondition(Filterable... filters) {
        filter = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        for (EffectResult effectResult : game.getActionsEnvironment().getTurnEffectResults()) {
            if (effectResult instanceof PlayCardResult playResult) {
                if (filter.accepts(game, playResult.getPlayedCard()))
                    return true;
            }
        }

        return false;

    }
}

package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.results.PlayCardResult;

public class CardPlayedInCurrentPhaseCondition implements Condition {
    private final Filter filter;

    public CardPlayedInCurrentPhaseCondition(Filterable... filters) {
        filter = Filters.and(filters);
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        for (EffectResult effectResult : game.getActionsEnvironment().getPhaseEffectResults()) {
            if (effectResult instanceof PlayCardResult playResult) {
                if (filter.accepts(game, playResult.getPlayedCard()))
                    return true;
            }
        }

        return false;

    }
}

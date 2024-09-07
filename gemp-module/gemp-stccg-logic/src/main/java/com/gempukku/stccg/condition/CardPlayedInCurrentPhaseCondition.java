package com.gempukku.stccg.condition;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardPlayedInCurrentPhaseCondition implements Condition {
    private final Filter filter;
    private final DefaultGame _game;

    public CardPlayedInCurrentPhaseCondition(ActionContext context, Filterable... filters) {
        filter = Filters.and(filters);
        _game = context.getGame();
    }

    @Override
    public boolean isFulfilled() {
        for (EffectResult effectResult : _game.getActionsEnvironment().getPhaseEffectResults()) {
            if (effectResult instanceof PlayCardResult playResult) {
                if (filter.accepts(_game, playResult.getPlayedCard()))
                    return true;
            }
        }

        return false;

    }
}

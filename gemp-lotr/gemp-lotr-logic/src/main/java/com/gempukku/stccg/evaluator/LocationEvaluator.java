package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class LocationEvaluator implements Evaluator {
    private final int defaultValue;
    private final int locationValue;
    private final Filter locationFilter;

    public LocationEvaluator(int defaultValue, int locationValue, Filterable... locationFilter) {
        this.defaultValue = defaultValue;
        this.locationValue = locationValue;
        this.locationFilter = Filters.and(locationFilter);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return locationFilter.accepts(game, game.getGameState().getCurrentSite()) ? locationValue : defaultValue;
    }
}

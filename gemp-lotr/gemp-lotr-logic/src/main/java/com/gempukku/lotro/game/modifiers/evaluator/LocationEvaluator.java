package com.gempukku.lotro.game.modifiers.evaluator;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

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
package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class Location implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final DefaultGame game = actionContext.getGame();
            final PhysicalCard currentSite = game.getGameState().getCurrentSite();
            return Filters.and(filterable).accepts(game, currentSite);
        };
    }
}

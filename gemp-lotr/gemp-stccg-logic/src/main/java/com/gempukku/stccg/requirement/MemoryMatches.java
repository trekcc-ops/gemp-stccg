package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;

public class MemoryMatches extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memory", "filter");

        final String memory = FieldUtils.getString(object.get("memory"), "memory");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return Filters.filter(cardsFromMemory, actionContext.getGame(), filterable).size() > 0;
        };
    }
}

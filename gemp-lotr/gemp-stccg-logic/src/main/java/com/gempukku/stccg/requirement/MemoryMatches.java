package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;

public class MemoryMatches extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "memory", "filter");

        final String memory = environment.getString(object.get("memory"), "memory");
        final String filter = environment.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return (actionContext) -> {
            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return !Filters.filter(cardsFromMemory, actionContext.getGame(), filterable).isEmpty();
        };
    }
}

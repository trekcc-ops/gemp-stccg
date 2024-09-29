package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;

public class MemoryMatches extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "memory", "filter");

        final String memory = node.get("memory").textValue();
        final String filter = node.get("filter").textValue();

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return (actionContext) -> {
            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return !Filters.filter(cardsFromMemory, actionContext.getGame(), filterable).isEmpty();
        };
    }
}

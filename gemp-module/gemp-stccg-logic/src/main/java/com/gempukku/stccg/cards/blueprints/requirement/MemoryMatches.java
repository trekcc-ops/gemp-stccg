package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;

public class MemoryMatches extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "memory", "filter");

        final String memory = node.get("memory").textValue();
        final String filter = node.get("filter").textValue();

        final FilterableSource filterableSource = new FilterFactory().generateFilter(filter);

        return (actionContext) -> {
            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return !Filters.filter(cardsFromMemory, actionContext.getGame(), filterable).isEmpty();
        };
    }
}
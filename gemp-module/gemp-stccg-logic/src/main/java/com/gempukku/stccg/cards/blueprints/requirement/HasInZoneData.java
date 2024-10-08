package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;

public class HasInZoneData extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("filter").textValue());

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            for (PhysicalCard physicalCard : Filters.filterActive(actionContext.getGame(), filterable)) {
                if (physicalCard.getWhileInZoneData() != null)
                    return true;
            }

            return false;
        };
    }
}
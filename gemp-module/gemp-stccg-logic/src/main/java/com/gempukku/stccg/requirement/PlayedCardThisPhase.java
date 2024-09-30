package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.CardPlayedInCurrentPhaseCondition;

public class PlayedCardThisPhase extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("filter").textValue());

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return new CardPlayedInCurrentPhaseCondition(actionContext, filterable).isFulfilled();
        };
    }
}

package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;

public class HasCardInDiscard extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "count", "filter");

        final int count = environment.getInteger(node, "count", 1);

        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(node.has("player") ? node.get("player").textValue() : "you");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("filter").textValue());
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerSource.getPlayerId(actionContext))
                    .hasCardInZone(Zone.DISCARD, count, filterable);
        };
    }
}
package com.gempukku.stccg.requirement.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;

public class HasCardInHand extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "count", "filter");

        final int count = node.get("count").asInt(1);
        final String filter = node.get("filter").textValue();

        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(node.has("player") ? node.get("player").textValue() : "you");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerSource.getPlayerId(actionContext))
                    .hasCardInZone(Zone.HAND, count, filterable);
        };
    }
}

package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;

public class HasCardInHand extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "player", "count", "filter");

        final int count = node.get("count").asInt(1);
        final String filter = node.get("filter").textValue();

        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(node.has("player") ? node.get("player").textValue() : "you");

        final FilterableSource filterableSource = new FilterFactory().generateFilter(filter);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerSource.getPlayerId(actionContext))
                    .hasCardInZone(Zone.HAND, count, filterable);
        };
    }
}
package com.gempukku.stccg.requirement.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;

public class HasCardInPlayPile extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "count", "filter");

        final String player = node.has("player") ? node.get("player").textValue() : "you";
        final int count = environment.getInteger(node, "count", 1);
        final String filter = node.get("filter").textValue();

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(filter);
        return (actionContext) -> {
            final String playerId = playerSource.getPlayerId(actionContext);
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return actionContext.getGameState().getPlayer(playerId).hasCardInZone(Zone.PLAY_PILE, count, filterable);
        };
    }
}

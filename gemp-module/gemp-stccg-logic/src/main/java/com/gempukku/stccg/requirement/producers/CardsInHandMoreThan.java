package com.gempukku.stccg.requirement.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;

public class CardsInHandMoreThan extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "count", "player");

        final int count = node.get("count").asInt();

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(node.get("player").textValue());

        return (actionContext) -> {
            final String playerId = playerSource.getPlayerId(actionContext);
            return actionContext.getGameState().getHand(playerId).size() > count;
        };
    }
}

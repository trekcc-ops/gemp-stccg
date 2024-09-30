package com.gempukku.stccg.requirement.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;

public class CardsInDeckCount extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "deck", "count");
        final String deck = node.has("deck") ? node.get("deck").textValue() : "you";

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("count"), environment);

        return actionContext -> {
            final String player = playerSource.getPlayerId(actionContext);
            final int count = valueSource.evaluateExpression(actionContext, null);

            return actionContext.getGameState().getDrawDeck(player).size() == count;
        };
    }
}

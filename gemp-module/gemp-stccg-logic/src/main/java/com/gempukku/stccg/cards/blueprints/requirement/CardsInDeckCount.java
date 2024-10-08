package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class CardsInDeckCount extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "deck", "count");
        final String deck = node.has("deck") ? node.get("deck").textValue() : "you";

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("count"));

        return actionContext -> {
            final String player = playerSource.getPlayerId(actionContext);
            final int count = valueSource.evaluateExpression(actionContext, null);

            return actionContext.getGameState().getDrawDeck(player).size() == count;
        };
    }
}
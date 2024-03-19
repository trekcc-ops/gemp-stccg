package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class CardsInDeckCount extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "deck", "count");

        final String deck = environment.getString(object.get("deck"), "deck", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("count"), environment);

        return actionContext -> {
            final String player = playerSource.getPlayerId(actionContext);
            final int count = valueSource.evaluateExpression(actionContext, null);

            return actionContext.getGameState().getDrawDeck(player).size() == count;
        };
    }
}

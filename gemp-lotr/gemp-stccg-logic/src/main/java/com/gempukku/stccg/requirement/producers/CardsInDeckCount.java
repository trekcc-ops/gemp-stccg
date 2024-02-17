package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class CardsInDeckCount extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "deck", "count");

        final String deck = FieldUtils.getString(object.get("deck"), "deck", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("count"), environment);

        return actionContext -> {
            final String player = playerSource.getPlayerId(actionContext);
            final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            return actionContext.getGame().getGameState().getDrawDeck(player).size() == count;
        };
    }
}

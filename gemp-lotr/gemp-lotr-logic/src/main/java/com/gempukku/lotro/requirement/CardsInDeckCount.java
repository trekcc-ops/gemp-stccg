package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class CardsInDeckCount implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "deck", "count");

        final String deck = FieldUtils.getString(object.get("deck"), "deck", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("count"), environment);

        return actionContext -> {
            final String player = playerSource.getPlayer(actionContext);
            final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            return actionContext.getGame().getGameState().getDeck(player).size() == count;
        };
    }
}

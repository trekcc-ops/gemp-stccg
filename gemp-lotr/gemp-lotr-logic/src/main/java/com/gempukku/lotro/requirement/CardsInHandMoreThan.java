package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class CardsInHandMoreThan implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "count", "player");

        final int count = FieldUtils.getInteger(object.get("count"), "count");
        final String player = FieldUtils.getString(object.get("player"), "player");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> {
            final String playerId = playerSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getHand(playerId).size() > count;
        };
    }
}

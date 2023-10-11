package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class CardsInHandMoreThan extends RequirementProducer {
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

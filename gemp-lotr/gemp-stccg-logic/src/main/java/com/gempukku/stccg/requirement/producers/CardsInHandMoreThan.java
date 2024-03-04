package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class CardsInHandMoreThan extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "count", "player");

        final int count = environment.getInteger(object.get("count"), "count");
        final String player = environment.getString(object.get("player"), "player");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return (actionContext) -> {
            final String playerId = playerSource.getPlayerId(actionContext);
            return actionContext.getGameState().getHand(playerId).size() > count;
        };
    }
}

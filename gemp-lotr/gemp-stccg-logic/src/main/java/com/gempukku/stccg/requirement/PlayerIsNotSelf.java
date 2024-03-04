package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class PlayerIsNotSelf extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "memory");

        final String memory = environment.getString(object.get("memory"), "memory");
        final PlayerSource selfPlayerSource = PlayerResolver.resolvePlayer("you");

        return (actionContext) -> {
            String selfPlayerId = selfPlayerSource.getPlayerId(actionContext);
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && !valueFromMemory.equals(selfPlayerId);
        };
    }
}

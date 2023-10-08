package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class PlayerIsNotSelf implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memory");

        final String memory = FieldUtils.getString(object.get("memory"), "memory");
        final PlayerSource selfPlayerSource = PlayerResolver.resolvePlayer("you");

        return (actionContext) -> {
            String selfPlayerId = selfPlayerSource.getPlayer(actionContext);
            String valueFromMemory = actionContext.getValueFromMemory(memory);
            return valueFromMemory != null && !valueFromMemory.equals(selfPlayerId);
        };
    }
}

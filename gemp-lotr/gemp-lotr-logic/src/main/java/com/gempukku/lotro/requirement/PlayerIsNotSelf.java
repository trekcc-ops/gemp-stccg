package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
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

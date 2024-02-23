package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class PlayerGoesOut implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);
        PlayerSource playerSource = PlayerResolver.resolvePlayer("you");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.playerGoesOut(actionContext.getEffectResult(),
                        playerSource.getPlayerId(actionContext));
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}

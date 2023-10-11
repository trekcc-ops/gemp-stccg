package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class StartOfTurn implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.startOfTurn(actionContext.getEffectResult());
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}

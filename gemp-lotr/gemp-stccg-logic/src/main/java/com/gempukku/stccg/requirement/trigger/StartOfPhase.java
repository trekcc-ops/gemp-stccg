package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class StartOfPhase implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase");
        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.startOfPhase(actionContext.getGame(), actionContext.getEffectResult(), phase);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}

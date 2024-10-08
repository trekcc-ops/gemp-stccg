package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Phase;

public class EndOfPhase implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "phase");
        final Phase phase = BlueprintUtils.getEnum(Phase.class, value.get("phase").textValue(), "phase");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.endOfPhase(actionContext.getGame(), actionContext.getEffectResult(), phase);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
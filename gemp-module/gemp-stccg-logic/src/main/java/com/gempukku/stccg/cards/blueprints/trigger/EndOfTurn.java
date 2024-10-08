package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class EndOfTurn implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.endOfTurn(actionContext.getEffectResult());
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
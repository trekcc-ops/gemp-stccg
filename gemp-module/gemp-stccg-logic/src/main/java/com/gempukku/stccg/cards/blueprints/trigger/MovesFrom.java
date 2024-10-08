package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;

public class MovesFrom implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter");

        final FilterableSource filterableSource = environment.getFilterable(value, "any");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.movesFrom(actionContext.getGame(), actionContext.getEffectResult(), filterableSource.getFilterable(actionContext));
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;

public class MovesFrom implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "filter");

        final FilterableSource filterableSource = BlueprintUtils.getFilterable(value, "any");

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
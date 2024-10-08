package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;

public class AboutToDiscardFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "source", "filter");

        String source = BlueprintUtils.getString(value, "source", "any");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source);
        final FilterableSource affectedFilter = BlueprintUtils.getFilterable(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.isGettingDiscardedBy(actionContext.getEffect(), actionContext.getGame(),
                        sourceFilter.getFilterable(actionContext),
                        affectedFilter.getFilterable(actionContext));
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class AboutToDiscardFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "source", "filter");

        String source = environment.getString(value.get("source"), "source", "any");
        String filter = environment.getString(value.get("filter"), "filter");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source);
        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter);

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

package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class Transferred implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "to");

        final String filter = environment.getString(value.get("filter"), "filter", "any");
        final String toFilter = environment.getString(value.get("to"), "to", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        final FilterableSource toFilterableSource = environment.getFilterFactory().generateFilter(toFilter);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.transferredCard(actionContext.getGame(),
                        actionContext.getEffectResult(),
                        filterableSource.getFilterable(actionContext),
                        null,
                        toFilterableSource.getFilterable(actionContext));
            }
        };
    }
}

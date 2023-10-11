package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class AboutToDiscardFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "source", "filter");

        String source = FieldUtils.getString(value.get("source"), "source", "any");
        String filter = FieldUtils.getString(value.get("filter"), "filter");

        final FilterableSource sourceFilter = environment.getFilterFactory().generateFilter(source, environment);
        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

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

package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TriggerConditions;
import org.json.simple.JSONObject;

public class Transferred implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "to");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String toFilter = FieldUtils.getString(value.get("to"), "to", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource toFilterableSource = environment.getFilterFactory().generateFilter(toFilter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                return TriggerConditions.transferredCard(actionContext.getGame(),
                        actionContext.getEffectResult(),
                        filterableSource.getFilterable(actionContext),
                        null,
                        toFilterableSource.getFilterable(actionContext));
            }
        };
    }
}

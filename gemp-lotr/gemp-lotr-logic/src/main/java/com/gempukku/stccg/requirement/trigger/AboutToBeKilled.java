package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class AboutToBeKilled implements TriggerCheckerProducer {
    @Override
    public TriggerChecker<DefaultGame> getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value,  "filter");

        String filter = FieldUtils.getString(value.get("filter"), "filter");

        final FilterableSource<DefaultGame> affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable affected = affectedFilter.getFilterable(actionContext);
                return TriggerConditions.isGettingKilled(actionContext.getEffect(), actionContext.getGame(), affected);
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}

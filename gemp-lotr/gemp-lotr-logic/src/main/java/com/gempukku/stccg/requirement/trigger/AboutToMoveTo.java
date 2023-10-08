package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class AboutToMoveTo implements TriggerCheckerProducer {
    @Override
    public TriggerChecker<DefaultGame> getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        final FilterableSource<DefaultGame> filterableSource =
                environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                return TriggerConditions.isMovingTo(actionContext.getEffect(), actionContext.getGame(), filterableSource.getFilterable(actionContext));
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}

package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.CardPlayedInCurrentPhaseCondition;
import org.json.simple.JSONObject;

public class PlayedCardThisPhase extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter");

        final String filter = environment.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return new CardPlayedInCurrentPhaseCondition(actionContext, filterable).isFulfilled();
        };
    }
}

package com.gempukku.stccg.requirement.producers;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementProducer;
import org.json.simple.JSONObject;

public class HasInZoneData extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter");

        final String filter = environment.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            for (PhysicalCard physicalCard : Filters.filterActive(actionContext.getGame(), filterable)) {
                if (physicalCard.getWhileInZoneData() != null)
                    return true;
            }

            return false;
        };
    }
}

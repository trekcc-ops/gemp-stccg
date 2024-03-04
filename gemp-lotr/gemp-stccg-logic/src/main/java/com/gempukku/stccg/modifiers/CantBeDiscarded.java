package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantBeDiscarded implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "by");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final FilterableSource byFilterableSource =
                environment.getFilterFactory().generateFilter(environment.getString(object.get("by"), "by", "any"));
        final Requirement[] requirements =
                environment.getRequirementsFromJSON(object);

        return (actionContext) -> new CantDiscardFromPlayModifier(actionContext.getSource(),
                "Can't be discarded",
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                byFilterableSource.getFilterable(actionContext));
    }
}

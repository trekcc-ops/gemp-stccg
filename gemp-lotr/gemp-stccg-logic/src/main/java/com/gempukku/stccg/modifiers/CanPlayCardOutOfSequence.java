package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CanPlayCardOutOfSequence implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires");

        final String filter = environment.getString(object.get("filter"), "filter");

        final Requirement[] requirements =
                environment.getRequirementsFromJSON(object);
        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(filter);

        return actionContext ->
                new CanPlayCardOutOfSequenceModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
    }
}

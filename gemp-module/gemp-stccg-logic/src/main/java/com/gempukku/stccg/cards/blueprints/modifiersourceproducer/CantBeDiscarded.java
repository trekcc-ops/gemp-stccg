package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;
import com.gempukku.stccg.requirement.Requirement;

public class CantBeDiscarded implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "by");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(object.get("filter").textValue());
        final FilterableSource byFilterableSource =
                environment.getFilterFactory().generateFilter(environment.getString(object, "by", "any"));
        final Requirement[] requirements =
                environment.getRequirementsFromJSON(object);

        return (actionContext) -> new CantDiscardFromPlayModifier(actionContext.getSource(),
                "Can't be discarded",
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                byFilterableSource.getFilterable(actionContext));
    }
}

package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;

public class CantBeDiscarded implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(object, "filter", "requires", "by");

        final FilterableSource filterableSource =
                new FilterFactory().generateFilter(object.get("filter").textValue());
        final FilterableSource byFilterableSource =
                new FilterFactory().generateFilter(BlueprintUtils.getString(object, "by", "any"));
        final Requirement[] requirements =
                RequirementFactory.getRequirements(object);

        return (actionContext) -> new CantDiscardFromPlayModifier(actionContext.getSource(),
                "Can't be discarded",
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                byFilterableSource.getFilterable(actionContext));
    }
}
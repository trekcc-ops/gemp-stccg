package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.requirement.Requirement;

public class CanPlayCardOutOfSequence implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter", "requires");

        final Requirement[] requirements = environment.getRequirementsFromJSON(node);
        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("filter").textValue());

        return actionContext ->
                new CanPlayCardOutOfSequenceModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
    }
}

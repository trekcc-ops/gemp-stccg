package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.modifiers.CancelStrengthBonusSourceModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;

public class CancelStrengthBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "from", "requires");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("from").textValue());
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return actionContext -> new CancelStrengthBonusSourceModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.modifiers.CancelStrengthBonusSourceModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;

public class CancelStrengthBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "from", "requires");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("from").textValue());
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return actionContext -> new CancelStrengthBonusSourceModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
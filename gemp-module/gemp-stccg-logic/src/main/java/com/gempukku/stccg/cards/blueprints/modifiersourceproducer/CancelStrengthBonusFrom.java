package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.modifiers.CancelStrengthBonusSourceModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;

public class CancelStrengthBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "from", "requires");

        final FilterableSource filterableSource =
                new FilterFactory().generateFilter(node.get("from").textValue());
        final Requirement[] requirements = new RequirementFactory().getRequirements(node);

        return actionContext -> new CancelStrengthBonusSourceModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
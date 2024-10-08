package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.modifiers.CancelStrengthBonusTargetModifier;

public class CancelStrengthBonusTo implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "filter", "from");

        final FilterableSource filterableSource =
                new FilterFactory().generateFilter(node.get("filter").textValue());
        final FilterableSource fromFilterableSource =
                new FilterFactory().generateFilter(node.get("from").textValue());

        return actionContext -> new CancelStrengthBonusTargetModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
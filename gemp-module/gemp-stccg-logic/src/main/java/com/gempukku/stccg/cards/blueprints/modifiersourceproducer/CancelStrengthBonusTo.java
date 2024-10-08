package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.modifiers.CancelStrengthBonusTargetModifier;

public class CancelStrengthBonusTo implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "filter", "from");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(node.get("filter").textValue());
        final FilterableSource fromFilterableSource =
                environment.getFilterFactory().generateFilter(node.get("from").textValue());

        return actionContext -> new CancelStrengthBonusTargetModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
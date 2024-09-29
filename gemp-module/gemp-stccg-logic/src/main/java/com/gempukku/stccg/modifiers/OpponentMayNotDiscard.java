package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;

public class OpponentMayNotDiscard implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter");

        final FilterableSource filterableSource = environment.getFilterable(object);

        return (actionContext) -> new CantDiscardFromPlayByPlayerModifier(
                actionContext.getSource(), "Can't be discarded by opponent",
                filterableSource.getFilterable(actionContext), actionContext.getPerformingPlayerId());
    }
}

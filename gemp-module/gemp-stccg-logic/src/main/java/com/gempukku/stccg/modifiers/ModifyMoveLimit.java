package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

public class ModifyMoveLimit implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return (actionContext) -> new MoveLimitModifier(actionContext.getSource(),
                valueSource.evaluateExpression(actionContext, null));
    }
}

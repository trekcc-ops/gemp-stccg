package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;

public class HasToMoveIfAble implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node);

        return actionContext ->
                new SpecialFlagModifier(actionContext.getSource(), ModifierFlag.HAS_TO_MOVE_IF_POSSIBLE);
    }
}

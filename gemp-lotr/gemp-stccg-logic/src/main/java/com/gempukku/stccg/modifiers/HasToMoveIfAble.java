package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import org.json.simple.JSONObject;

public class HasToMoveIfAble implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object);

        return actionContext -> new SpecialFlagModifier(actionContext.getSource(), ModifierFlag.HAS_TO_MOVE_IF_POSSIBLE);
    }
}

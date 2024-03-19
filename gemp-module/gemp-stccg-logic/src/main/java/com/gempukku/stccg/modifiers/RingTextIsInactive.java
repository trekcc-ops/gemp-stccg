package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import org.json.simple.JSONObject;

public class RingTextIsInactive implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object);

        return actionContext -> new SpecialFlagModifier(actionContext.getSource(), ModifierFlag.RING_TEXT_INACTIVE);
    }
}

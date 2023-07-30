package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierFlag;
import com.gempukku.lotro.modifiers.SpecialFlagModifier;
import org.json.simple.JSONObject;

public class RingTextIsInactive implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object);

        return actionContext -> new SpecialFlagModifier(actionContext.getSource(), ModifierFlag.RING_TEXT_INACTIVE);
    }
}

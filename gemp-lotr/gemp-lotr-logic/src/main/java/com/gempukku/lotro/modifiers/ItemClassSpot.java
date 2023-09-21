package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.modifiers.lotronly.PossessionClassSpotModifier;
import org.json.simple.JSONObject;

public class ItemClassSpot implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "class");

        final PossessionClass spotClass = FieldUtils.getEnum(PossessionClass.class, object.get("class"), "class");

        return actionContext -> new PossessionClassSpotModifier(actionContext.getSource(), spotClass);
    }
}

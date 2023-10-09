package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.filterable.PossessionClass;
import org.json.simple.JSONObject;

public class ItemClassSpot implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "class");

        final PossessionClass spotClass = FieldUtils.getEnum(PossessionClass.class, object.get("class"), "class");

        return actionContext -> new PossessionClassSpotModifier(actionContext.getSource(), spotClass);
    }
}

package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.common.filterable.lotr.PossessionClass;
import org.json.simple.JSONObject;

public class ItemClassSpot implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "class");

        final PossessionClass spotClass = environment.getEnum(PossessionClass.class, object.get("class"), "class");

        return actionContext -> new PossessionClassSpotModifier(actionContext.getSource(), spotClass);
    }
}

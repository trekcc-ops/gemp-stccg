package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class ModifierProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "modifier");

        JSONObject jsonObject = (JSONObject) value.get("modifier");
        final ModifierSource modifier = environment.getModifierSourceFactory().getModifier(jsonObject, environment);

        blueprint.appendInPlayModifier(modifier);
    }
}

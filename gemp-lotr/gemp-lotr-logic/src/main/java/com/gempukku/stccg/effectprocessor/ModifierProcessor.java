package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ModifierProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "modifier");

        JSONObject jsonObject = (JSONObject) value.get("modifier");
        final ModifierSource modifier = environment.getModifierSourceFactory().getModifier(jsonObject, environment);

        blueprint.appendInPlayModifier(modifier);
    }
}

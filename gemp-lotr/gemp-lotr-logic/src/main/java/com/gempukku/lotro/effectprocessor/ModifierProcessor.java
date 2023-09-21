package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
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

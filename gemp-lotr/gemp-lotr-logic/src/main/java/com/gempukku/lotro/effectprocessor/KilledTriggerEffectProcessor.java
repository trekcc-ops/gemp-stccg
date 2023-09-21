package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.actions.DefaultActionSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class KilledTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "requires", "cost", "effect");

        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);

        DefaultActionSource triggerActionSource = new DefaultActionSource();
        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);
        if (optional)
            blueprint.setKilledOptionalTriggerAction(triggerActionSource);
        else
            blueprint.setKilledRequiredTriggerAction(triggerActionSource);
    }
}

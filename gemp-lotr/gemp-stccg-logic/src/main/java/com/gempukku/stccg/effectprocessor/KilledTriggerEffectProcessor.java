package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class KilledTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
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

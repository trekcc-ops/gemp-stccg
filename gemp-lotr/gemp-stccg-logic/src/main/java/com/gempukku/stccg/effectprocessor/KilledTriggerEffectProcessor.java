package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.actions.sources.RequiredTriggerActionSource;
import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class KilledTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "requires", "cost", "effect");

        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);

        RequiredType requiredType;
        ActionSource triggerActionSource;

        if (optional) {
            requiredType = RequiredType.OPTIONAL;
            triggerActionSource = new OptionalTriggerActionSource();
        } else {
            requiredType = RequiredType.REQUIRED;
            triggerActionSource = new RequiredTriggerActionSource();
        }

        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);
        if (optional)
            blueprint.setKilledTrigger(requiredType, triggerActionSource);
        else
            blueprint.setKilledTrigger(requiredType, triggerActionSource);
    }
}

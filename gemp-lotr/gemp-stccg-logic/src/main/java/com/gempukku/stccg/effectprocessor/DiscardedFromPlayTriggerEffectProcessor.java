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

public class DiscardedFromPlayTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "requires", "cost", "effect");

        RequiredType requiredType = FieldUtils.getBoolean(value.get("optional"), "optional", false) ?
                RequiredType.OPTIONAL : RequiredType.REQUIRED;

        ActionSource triggerActionSource = requiredType == RequiredType.OPTIONAL ?
                new OptionalTriggerActionSource() : new RequiredTriggerActionSource();
        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);
        blueprint.setDiscardedFromPlayTrigger(requiredType, triggerActionSource);
    }
}

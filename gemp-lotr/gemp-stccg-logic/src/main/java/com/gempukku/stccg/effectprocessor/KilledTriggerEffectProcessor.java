package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.actions.sources.RequiredTriggerActionSource;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.RequiredType;
import org.json.simple.JSONObject;

public class KilledTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "optional", "requires", "cost", "effect");

        final boolean optional = environment.getBoolean(value.get("optional"), "optional", false);

        RequiredType requiredType;
        ActionSource triggerActionSource;

        if (optional) {
            requiredType = RequiredType.OPTIONAL;
            triggerActionSource = new OptionalTriggerActionSource();
        } else {
            requiredType = RequiredType.REQUIRED;
            triggerActionSource = new RequiredTriggerActionSource();
        }

        triggerActionSource.processRequirementsCostsAndEffects(value, environment);
        blueprint.setKilledTrigger(requiredType, triggerActionSource);
    }
}

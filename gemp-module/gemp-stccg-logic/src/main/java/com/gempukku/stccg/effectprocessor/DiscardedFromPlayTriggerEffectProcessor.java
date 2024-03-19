package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.actions.sources.RequiredTriggerActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.RequiredType;
import org.json.simple.JSONObject;

public class DiscardedFromPlayTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "optional", "requires", "cost", "effect");

        RequiredType requiredType = environment.getBoolean(value.get("optional"), "optional", false) ?
                RequiredType.OPTIONAL : RequiredType.REQUIRED;

        ActionSource triggerActionSource = requiredType == RequiredType.OPTIONAL ?
                new OptionalTriggerActionSource() : new RequiredTriggerActionSource();
        triggerActionSource.processRequirementsCostsAndEffects(value, environment);
        blueprint.setDiscardedFromPlayTrigger(requiredType, triggerActionSource);
    }
}

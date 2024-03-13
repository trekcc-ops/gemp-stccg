package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

public class InHandTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "trigger", "requires", "cost", "effect");

        final JSONObject[] triggerArray = environment.getObjectArray(value.get("trigger"), "trigger");

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();
            if (before)
                throw new InvalidCardDefinitionException("Only after triggers from hand are supported");

            // TODO - Assumes it is optional
            OptionalTriggerActionSource triggerActionSource = new OptionalTriggerActionSource();
            triggerActionSource.addRequirement(triggerChecker);
            triggerActionSource.processRequirementsCostsAndEffects(value, environment);
            blueprint.appendOptionalInHandTrigger(triggerActionSource, TriggerTiming.AFTER);
        }
    }
}

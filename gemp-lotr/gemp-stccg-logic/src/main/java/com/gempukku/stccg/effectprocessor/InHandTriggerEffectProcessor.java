package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

public class InHandTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "requires", "cost", "effect");

        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();
            if (before)
                throw new InvalidCardDefinitionException("Only after triggers from hand are supported");

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            blueprint.appendOptionalInHandAfterTrigger(triggerActionSource);
        }
    }
}

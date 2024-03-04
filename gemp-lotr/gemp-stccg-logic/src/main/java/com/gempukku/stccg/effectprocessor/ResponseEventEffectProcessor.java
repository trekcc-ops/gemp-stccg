package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

public class ResponseEventEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "trigger", "requires", "cost", "effect");

        final JSONObject[] triggerArray = environment.getObjectArray(value.get("trigger"), "trigger");

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker =
                    environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            triggerActionSource.addRequirement(triggerChecker);
            triggerActionSource.processRequirementsCostsAndEffects(value, environment);
            blueprint.appendOptionalInHandTrigger(
                    triggerActionSource, triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER);
        }
    }
}

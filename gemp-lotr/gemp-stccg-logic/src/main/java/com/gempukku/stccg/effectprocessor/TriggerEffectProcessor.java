package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.*;
import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

import java.util.Objects;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "optional", "requires", "cost", "effect", "text");

        final String text = FieldUtils.getString(value.get("text"), "text", "");
        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");
        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");
        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);

        for (JSONObject trigger : triggerArray) {
            TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);

            TriggerTiming triggerTiming = triggerChecker.isBefore() ?
                            TriggerTiming.BEFORE : TriggerTiming.AFTER;

            TriggerActionSource triggerActionSource;

            if (optional) {
                triggerActionSource = new OptionalTriggerActionSource(triggerTiming);
            } else {
                triggerActionSource = new RequiredTriggerActionSource(triggerTiming);
            }

            if (!Objects.equals(text, "")) {
                triggerActionSource.setText(text);
            }
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);
            blueprint.appendBeforeOrAfterTrigger(triggerActionSource);
        }
    }

}

package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

import java.util.Objects;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "optional", "requires", "cost", "effect", "text");

        final String text = FieldUtils.getString(value.get("text"), "text", "");
        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");
        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");
        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            if(!Objects.equals(text, "")) {
                triggerActionSource.setText(text);
            }
            triggerActionSource.addPlayRequirement(triggerChecker);
//            LOGGER.debug("fullObject = " + value.toString());
            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            RequiredType requiredType = optional ? RequiredType.OPTIONAL : RequiredType.REQUIRED;

            if (before) {
                blueprint.appendBeforeTrigger(requiredType, triggerActionSource);
            } else {
                blueprint.appendAfterTrigger(requiredType, triggerActionSource);
            }
        }
    }
}

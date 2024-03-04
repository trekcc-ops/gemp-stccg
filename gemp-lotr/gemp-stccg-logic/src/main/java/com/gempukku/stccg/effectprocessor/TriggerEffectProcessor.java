package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.sources.*;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.ActionLimitType;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

import java.util.Objects;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "trigger", "optional", "requires", "cost", "effect", "text", "limit");

        final String text = environment.getString(value.get("text"), "text", "");
        final JSONObject[] triggerArray = environment.getObjectArray(value.get("trigger"), "trigger");
        if (triggerArray.length == 0)
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");
        final boolean optional = environment.getBoolean(value.get("optional"), "optional", false);
        final ActionLimitType limitType = environment.getEnum(
                ActionLimitType.class, environment.getString(value.get("limit"), "limit", "unlimited"));

        for (JSONObject trigger : triggerArray) {
            TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);

            TriggerTiming triggerTiming = triggerChecker.isBefore() ?
                            TriggerTiming.BEFORE : TriggerTiming.AFTER;

            TriggerActionSource triggerActionSource;

            if (optional) {
                triggerActionSource = new OptionalTriggerActionSource(triggerTiming, limitType);
            } else {
                triggerActionSource = new RequiredTriggerActionSource(triggerTiming);
            }

            if (!Objects.equals(text, "")) {
                triggerActionSource.setText(text);
            }
            triggerActionSource.addRequirement(triggerChecker);
            triggerActionSource.processRequirementsCostsAndEffects(value, environment);
            blueprint.appendBeforeOrAfterTrigger(triggerActionSource);
        }
    }

}

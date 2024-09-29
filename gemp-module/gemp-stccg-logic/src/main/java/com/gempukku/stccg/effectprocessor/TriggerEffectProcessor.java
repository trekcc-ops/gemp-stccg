package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.sources.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.ActionLimitType;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.Objects;

public class TriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node,
                "trigger", "optional", "requires", "cost", "effect", "text", "limit");

        final String text = environment.getString(node, "text", "");

        final boolean optional = environment.getBoolean(node, "optional", false);
        final ActionLimitType limitType = environment.getEnum(
                ActionLimitType.class, environment.getString(node, "limit", "unlimited"));

        if (!node.has("trigger"))
            throw new InvalidCardDefinitionException("Trigger effect without trigger definition");

        if (node.get("trigger").isArray()) {
            for (JsonNode trigger : node.get("trigger"))
                appendTrigger(trigger, node, blueprint, environment, optional, limitType, text);
        } else {
            appendTrigger(node.get("trigger"), node, blueprint, environment, optional, limitType, text);
        }
    }

    private void appendTrigger(JsonNode trigger, JsonNode parentNode, CardBlueprint blueprint,
                               CardBlueprintFactory environment, boolean optional, ActionLimitType limitType,
                               String text) throws InvalidCardDefinitionException {
        TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
        TriggerTiming triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
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
        triggerActionSource.processRequirementsCostsAndEffects(parentNode, environment);
        blueprint.appendBeforeOrAfterTrigger(triggerActionSource);
    }
}
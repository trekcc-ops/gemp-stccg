package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

public class InHandTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "trigger", "requires", "cost", "effect");

        if (node.has("trigger")) {
            if (node.get("trigger").isArray()) {
                for (JsonNode trigger : node.get("trigger"))
                    appendTrigger(node, trigger, blueprint, environment);
            } else {
                appendTrigger(node, node.get("trigger"), blueprint, environment);
            }
        }
    }

    private void appendTrigger(JsonNode parentNode, JsonNode trigger, CardBlueprint blueprint,
                               CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final TriggerChecker triggerChecker =
                environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
        final boolean before = triggerChecker.isBefore();
        if (before)
            throw new InvalidCardDefinitionException("Only after triggers from hand are supported");

        // TODO - Assumes it is optional
        OptionalTriggerActionSource triggerActionSource = new OptionalTriggerActionSource();
        triggerActionSource.addRequirement(triggerChecker);
        triggerActionSource.processRequirementsCostsAndEffects(parentNode, environment);
        blueprint.appendOptionalInHandTrigger(triggerActionSource, TriggerTiming.AFTER);
    }
}

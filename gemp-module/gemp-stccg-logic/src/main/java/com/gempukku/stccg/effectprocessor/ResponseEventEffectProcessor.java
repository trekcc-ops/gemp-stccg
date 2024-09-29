package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

public class ResponseEventEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "trigger", "requires", "cost", "effect");

        if (node.get("trigger").isArray()) {
            for (JsonNode trigger : node.get("trigger")) {
                appendTrigger(trigger, node, blueprint, environment);
            }
        } else {
            appendTrigger(node.get("trigger"), node, blueprint, environment);
        }

    }

    private void appendTrigger(JsonNode trigger, JsonNode parentNode, CardBlueprint blueprint,
                               CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final TriggerChecker triggerChecker =
                environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);

        DefaultActionSource triggerActionSource = new DefaultActionSource();
        triggerActionSource.addRequirement(triggerChecker);
        triggerActionSource.processRequirementsCostsAndEffects(parentNode, environment);
        blueprint.appendOptionalInHandTrigger(
                triggerActionSource, triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER);
    }
}

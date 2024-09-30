package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.actionsource.*;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.LinkedList;
import java.util.List;

public class ActionSourceAppender implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node,
                "text", "optional", "limitPerPhase", "limitPerTurn", "phase", "trigger",
                "requires", "cost", "effect");

        List<DefaultActionSource> actionSourceList = new LinkedList<>();
        boolean isResponse = node.has("trigger");

        String text = environment.getString(node, "text");
        final boolean optional = environment.getBoolean(node, "optional", false);
        final int limitPerPhase = environment.getInteger(node, "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(node, "limitPerTurn", 0);
        final Phase phase = environment.getEnum(Phase.class, environment.getString(node, "phase"), "phase");

        if (!isResponse) {
            actionSourceList.add(new ActivateCardActionSource());
       } else {
            List<JsonNode> triggers = JsonUtils.toArray(node.get("trigger"));
            for (JsonNode trigger : triggers) {
                TriggerChecker triggerChecker =
                        environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
                TriggerTiming triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
                TriggerActionSource triggerActionSource;
                if (optional) {
                    triggerActionSource = new OptionalTriggerActionSource(triggerTiming);
                } else {
                    triggerActionSource = new RequiredTriggerActionSource(triggerTiming);
                }
                triggerActionSource.addRequirement(triggerChecker);
                actionSourceList.add(triggerActionSource);
            }
        }

        for (DefaultActionSource actionSource : actionSourceList) {
            if (text != null)
                actionSource.setText(text);
            if (limitPerPhase > 0)
                actionSource.setPhaseLimit(phase, limitPerPhase);
            if (limitPerTurn > 0)
                actionSource.setTurnLimit(limitPerTurn);
            if (phase != null)
                actionSource.addRequirement(
                        (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
            actionSource.processRequirementsCostsAndEffects(node, environment);

            if (!isResponse)
                blueprint.appendInPlayPhaseAction(actionSource);
            else if (actionSource instanceof TriggerActionSource triggerSource)
                blueprint.appendBeforeOrAfterTrigger(triggerSource);
            else throw new InvalidCardDefinitionException("Something went wrong while adding adding action source");
        }
    }
}
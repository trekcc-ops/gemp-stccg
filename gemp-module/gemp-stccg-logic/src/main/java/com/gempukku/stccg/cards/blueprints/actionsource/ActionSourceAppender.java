package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerChecker;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;

import java.util.LinkedList;
import java.util.List;

public class ActionSourceAppender {
    public void processEffect(JsonNode node, CardBlueprint blueprint)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node,
                "text", "optional", "limitPerPhase", "limitPerTurn", "phase", "trigger",
                "requires", "cost", "effect");

        List<DefaultActionSource> actionSourceList = new LinkedList<>();
        boolean isResponse = node.has("trigger");

        String text = BlueprintUtils.getString(node, "text");
        final boolean optional = BlueprintUtils.getBoolean(node, "optional", false);
        final int limitPerPhase = BlueprintUtils.getInteger(node, "limitPerPhase", 0);
        final int limitPerTurn = BlueprintUtils.getInteger(node, "limitPerTurn", 0);
        final Phase phase = BlueprintUtils.getEnum(Phase.class, BlueprintUtils.getString(node, "phase"), "phase");

        if (!isResponse) {
            actionSourceList.add(new ActivateCardActionSource());
       } else {
            List<JsonNode> triggers = JsonUtils.toArray(node.get("trigger"));
            for (JsonNode trigger : triggers) {
                TriggerChecker triggerChecker = new TriggerCheckerFactory().getTriggerChecker(trigger, new CardBlueprintFactory());
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
            actionSource.processRequirementsCostsAndEffects(node);

            if (!isResponse)
                blueprint.appendInPlayPhaseAction(actionSource);
            else if (actionSource instanceof TriggerActionSource triggerSource)
                blueprint.appendBeforeOrAfterTrigger(triggerSource);
            else throw new InvalidCardDefinitionException("Something went wrong while adding adding action source");
        }
    }
}
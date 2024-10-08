package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.actionsource.*;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerChecker;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EffectFieldProcessor implements FieldProcessor {

    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint)
            throws InvalidCardDefinitionException {
        List<JsonNode> effectList = JsonUtils.toArray(value);
        for (JsonNode effect : effectList) {
            final String effectType = effect.get("type").textValue().toLowerCase();
            switch (effectType) {
                case "action":
                    appendActionSource(effect, blueprint);
                    break;
                case "modifier":
                    BlueprintUtils.validateAllowedFields(effect, "modifier");
                    blueprint.appendInPlayModifier(BlueprintUtils.getModifier(effect.get("modifier")));
                    break;
                case "playoutofsequence":
                    BlueprintUtils.validateAllowedFields(effect, "requires");
                    final Requirement[] conditions = new RequirementFactory().getRequirements(effect);
                    blueprint.appendPlayOutOfSequenceCondition(actionContext -> actionContext.acceptsAllRequirements(conditions));
                    break;
                case "seed":
                    appendSeedActionSource(effect, blueprint);
                    break;
                default:
                    throw new InvalidCardDefinitionException("Unable to find effect of type: " + effectType);
            }
        }
    }

    private void appendActionSource(JsonNode node, CardBlueprint blueprint)
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
                TriggerChecker triggerChecker =
                        new TriggerCheckerFactory().getTriggerChecker(trigger);
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
            else throw new InvalidCardDefinitionException("Something went wrong while adding action source");
        }
    }

    private void appendSeedActionSource(JsonNode value, CardBlueprint blueprint)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "limit", "where");
        SeedCardActionSource actionSource = new SeedCardActionSource();
        if (value.has("limit"))
            actionSource.addRequirement((actionContext) -> actionContext.getSource()
                    .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) < value.get("limit").asInt());
        if (value.has("where")) {
            if (Objects.equals(value.get("where").textValue(), "table"))
                actionSource.setSeedZone(Zone.TABLE);
            else throw new InvalidCardDefinitionException("Unknown parameter in seed:where field");
        }
        blueprint.setSeedCardActionSource(actionSource);
    }
}